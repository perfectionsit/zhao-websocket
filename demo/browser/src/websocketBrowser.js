/**
 * 前端Websocket工具包，与后端springboot3.0的WebsocketServer配套使用
 */
let ws, token = [],requestCount = 0 ,closeType = '',path = '',reconnectTimer, pingTimer
let isClient = false
const dataBox = {}
const noticeFuncMap={}

/**
 * 添加通知响应映射方法，映射路径为方法名
 * @param func 映射方法
 * @param name 映射方法名
 */
export function addNoticeFunction(func,name){
    noticeFuncMap[name]=func
}

/**
 * 获取当前关闭连接类型
 * @returns （客户端登出|服务端踢出）
 */
export function getCloseType() {
    return closeType
}

/**
 * 设置Websocket连接路径，如ws://127.0.0.1:8080或wss://127.0.0.1:8080(注意要记得添加端口号)
 * @param _path
 */
export function setWebsocketPath(_path) {
    path = _path+'/zhao'
}

/**
 * 开始每隔5分钟发送一次心跳包
 */
function startPing() {
    closePing()
    pingTimer = setInterval(() => {
        ws.send(new Uint8Array([2]))
    }, 300000)
}

/**
 * 关闭发送心跳包计时
 */
function closePing() {
    pingTimer && clearInterval(pingTimer)
}

/**
 * 开始计时数据返回时间，如果返回时间大于30秒则重连
 */
function startTimer() {
    closeTimer()
    reconnectTimer = setTimeout(() => {
        ws.close()
    }, 30000)
}
/**
 * 关闭数据返回时间计时
 */
function closeTimer() {
    reconnectTimer && clearTimeout(reconnectTimer)
}

function watch(obj, propName, callback) {
    let value = obj[propName];
    Object.defineProperty(obj, propName, {
        configurable: true,
        get() {
            return value;
        },
        set(newValue) {
            value = newValue;
            callback();
        }
    });
}

/**
 * 发送socket请求，data与file可以并行发送
 * @param url 地址（使用方法名）
 * @param data 字符数据
 * @param file 文件数据
 * @returns {Promise<Object>} 后端响应数据
 */
export function socket(url, data = undefined, file = undefined) {
    return new Promise(async (resolve,reject) => {
        if(isClient){
            const count = (requestCount++&1073741823)+1
            const res = await requestEncode(url, count, data, file)
            for (let i = 0; i < res.length; i++) {
                startTimer()
                ws.send(new Uint8Array(res[i]));
            }
            watch(dataBox, count,() => {
                resolve(dataBox[count])
                delete dataBox[count]
            })
        }
        else{
            reject('WebSocket未连接')
        }

    })
}

/**
 * 关闭websocket连接，调用此方法时断开连接类型为”客户端登出“
 */
export async function closeSocket() {
    closeType = '客户端登出'
    ws.close()
}

let init = ()=>{}

/**
 * 当连接断开或者重连失败时的初始化函数
 * @param _init 初始化函数
 */
export function setInit(_init) {
    init = _init
}

/**
 *当连接断开或者重连失败时调用此函数
 */
function __init() {
    isClient = false
    requestCount = 0
    init()
    closeType = ''
}

/**
 * 连接断开时调用此函数
 */
async function onClose() {
    isClient = false
    closePing()
    if (closeType === "服务端踢出" || closeType === '客户端登出') {
        __init()
    } else {
        await reconnect()
    }
}

/**
 * 接收响应时调用此函数
 * @param res 相应数据
 */
async function onMessage(res) {
    closeType = '服务端踢出'
    closeTimer()
    if (res.data.size > 2) {
        const event = await responseDecode(res.data)
        if (event.responseType === 'response') {
            closeType = ''
            if (event.dataType === 'string') {
                try{
                    event.data = JSON.parse(event.data)
                }catch (e){}
            }
            dataBox[event.count] = event
        } else if (event.responseType === 'notice') {
            //后端通知响应时调用前端映射方法
            closeType = ''
            try{
                event.data = JSON.parse(event.data)
            }catch (e){
                throw new Error("响应数据解码失败，请检查响应数据类型"+e)
            }
            try{
                noticeFuncMap[event.data.status](event.data.data)
            }catch (e){
                throw new Error("通知方法错误，请检查方法是否加载成功")
            }
        } else {
            closeType = '服务端踢出'
        }
    }
}
/**
 * 新建websocket连接
 * @param _token : String 唯一token码（可以为空，不可超过127位）
 * @param _path 指定路径 （不为空时代替setWebsocketPath方法指定的路径）
 * @returns Promise<null> 异常捕获
 */
export function newSocket(_token = undefined, _path = undefined) {
    return new Promise(async (resolve, reject) => {
        if (typeof _path != 'undefined') {
            path = _path + '/zhao'
        }
        token = []
        if(typeof _token != 'undefined'){
            let __token=[]
            __token.push(...await new Uint8Array(await new Blob([_token]).arrayBuffer()))
            if(__token.length>127){
                reject()
                return
            }
            token.push(__token.length)
            token.push(...__token)
        }
        else{
            token=[0]
        }
        try {
            await connect()
        } catch (e) {
            try {
                await reconnect()
            } catch (e) {
                reject()
            }
        }
        resolve()
    })
}

/**
 * 将后端响应解码
 * @param data : Array
 * @returns {Promise<{responseType:String , count:Number ,dataType:String, data:String|Array}>}
 */
function responseDecode(data) {
    return new Promise(async resolve => {
        let information = data.slice(0, 6)
        let res = {}
        information = await new Uint8Array(await information.arrayBuffer())
        switch (information[0] & 0xff) {
            case 0:
                res.responseType = 'notice';
                break
            case 1:
                res.responseType = 'response';
                break
            default:
                res.responseType = 'close'
        }
        res.count = (information[1] & 0xff) + ((information[2] & 0xff) << 8) +
            ((information[3] & 0xff) << 16) + ((information[4] & 0xff) << 24)
        switch ((information[5] & 0xff)){
            case 0:
                res.dataType = 'bytes'
                res.data = data.slice(6)
                break
            case 1:
                let _data = data.slice(6)
                res.dataType = 'string'
                res.data = await _data.text()
                break
            case 2:
                res.dataType = 'null'
                res.data = null
        }
        resolve(res)
    })
}

/**
 * 将前端请求编码
 * @param method : String 将使用方法名
 * @param count : Number 请求标记
 * @param data : null|String 请求体
 * @param file : null|Array 比特数组文件
 * @returns {Promise<Array>}
 */
function requestEncode(method, count, data = undefined, file = undefined) {
    return new Promise(async resolve => {
        let request = []
        request.push(...token)
        let methodNameBytes = await new Uint8Array(await new Blob([method]).arrayBuffer())
        let _methodBytesLength = methodNameBytes.length
        for (let i = 0; i < 4; i++) {
            request.push(_methodBytesLength & 0xff)
            _methodBytesLength >>= 8
        }
        for (let i = 0; i < methodNameBytes.length; i++) {
            request.push(methodNameBytes[i])
        }
        if (typeof file === 'undefined') {
            request.push(0)
        } else {
            request.push(1)
            let _fileLength = file.length
            for (let i = 0; i < 4; i++) {
                request.push(_fileLength & 0xff)
                _fileLength >>= 8
            }
            for (let i = 0; i < file.length; i++) {
                request.push(file[i])
            }
        }
        if (typeof data === 'undefined') {
            request.push(0)
        } else {
            request.push(1)
            let res = await new Uint8Array(await new Blob([data]).arrayBuffer())
            let _dataLength = res.length
            for (let i = 0; i < 4; i++) {
                request.push(_dataLength & 0xff)
                _dataLength >>= 8
            }
            for (let i = 0; i < res.length; i++) {
                request.push(res[i])
            }
        }
        resolve(await splitPage(request,count))
    })
}

/**
 * 请求数据分页
 * @param data 请求数据
 * @param count 请求标记
 * @returns {Promise<Array>}
 */
function splitPage(data,count) {
    return new Promise(resolve => {
        const page = Math.ceil(data.length / 7168)
        let result = [[]]
        for (let i = 0; i < page; i++) {
            result[i] = [((i === page - 1) ? 1 : 0)]
            let _count = count
            for (let j = 0; j < 4; j++) {
                result[i].push(_count & 0xff)
                _count >>= 8
            }
            result[i].push(...data.slice(i * 7168, (i + 1) * 7168))
        }
        resolve(result)
    })
}

/**
 * 异步等待
 * @param time 等待时间（毫秒）
 */
function sleep(time) {
    return new Promise(resolve => {
        setTimeout(() => {
            resolve()
        }, time)
    })
}

/**
 * 连接websocket
 * @returns {Promise<null>} 异常捕获
 */
function connect() {
    return new Promise(async (resolve, reject) => {
        ws = new WebSocket(path)
        ws.onopen = async () => {
            isClient = true
            ws.onmessage = onMessage
            ws.onclose = onClose
            startPing()
            resolve()
            ws.send((new Uint8Array((await requestEncode("", requestCount))[0])))
            window.onbeforeunload = () => {
                closeSocket()
            }
        }
        ws.onclose = () => {
            isClient = false
            reject()
        }
    })
}

/**
 * websocket重连
 * @returns {Promise<null>} 异常捕获
 */
function reconnect() {
    return new Promise(async (resolve, reject) => {
        for (let i = 1000; i <= 256000; i <<= 1) {
            await sleep(i)
            try {
                await connect()
                resolve()
                break
            } catch (e) {
            }
        }
        if (!isClient) {
            __init()
            reject()
        }
    })
}