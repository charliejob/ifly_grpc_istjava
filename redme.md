一、开发环境
    intellij idea
    oracle jdk 1.8
二、项目说明
    本项目是google grpc协议客户端，协议参数参考《实时转写（ist）开发文档.pdf》
三、代码说明
    main：代码目录根据参数生成的grpc客户端。
    test：代码为调用客户端的的示例代码。
        com.iflytek.vcp.voice.engine.ist.client.IstClientTest.main是入口方法，请根据需求修改url、audioFile、sampleRate参数值