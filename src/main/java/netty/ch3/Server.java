package netty.ch3;


import io.netty.bootstrap.AbstractBootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import netty.ch6.AuthHandler;

/**
 * @author
 */
public final class Server {

    public static void main(String[] args) throws Exception {
      /*
      NioEventLoop  就是启了两种线程
        1.监听客户端连接
        2.进行客户端读写
       */

        //1.启动 监听客户端连接 的线程
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //2.启动 进行客户端读写 的线程
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            //ServerBootstrap是辅助类
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    //设置服务端的socketChannel
                    .channel(NioServerSocketChannel.class)
                    //给后面指定的每一个客户端的连接设置一些tcp的基本属性
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    //每次创建客户端连接，绑定一些基本属性
                    .childAttr(AttributeKey.newInstance("childAttr"), "childAttrValue")
                    //handler  定义服务端启动过程中有一些什么样的逻辑
                    .handler(new ServerHandler())
                    //
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new AuthHandler());
                            //..
                            //这里配置很多的Handler

                        }
                    });
            //绑定一个端口，这行代码之后server端就启动起来了

            //bind方法为服务端channel创建的入口
            //AbstractBootstrap.initAndRegister-->
            //ReflectiveChannelFactory.newChannel();通过反射创建channel
            // 也就是“ .channel(NioServerSocketChannel.class)”这行代码指定的 NioServerSocketChannel


//            下面是 NioServerSocketChannel的构造方法
//                public NioServerSocketChannel() {
//                this(newSocket(DEFAULT_SELECTOR_PROVIDER));  //这里调用newSocket，通过JDK的（java.nio.channels.spi.SelectorProvider.openServerSocketChannel）来创建JDK_channel （java.nio.channels.ServerSocketChannel）
//            }

//            然后调用 this.config = new NioServerSocketChannel.NioServerSocketChannelConfig(this, this.javaChannel().socket());   NioServerSocketChannelConfig为 tcp参数配置类

//            下面是设置Channel为非阻塞
//             protected AbstractNioChannel(Channel parent, SelectableChannel ch, int readInterestOp) {
//                super(parent);
//                this.ch = ch;
//                this.readInterestOp = readInterestOp;
//
//                try {
//                    ch.configureBlocking(false);


//            服务端的channe和客户端的channel都是最终继承AbstractChannel，AbstractChannel，就是channel的一个抽象，不管是服务端还是客户端的channel，都有一个id,unsafe,pipeline三个属性
//            id队一行每一个channel的唯一标识
//                    unsafe跟TCP相关的读写，底层的一个操作，netty自己用到的一个类，不建议大家拿来直接用
//                    pipeline客户端与服务端逻辑相关的一个逻辑链
//                protected AbstractChannel(Channel parent) {
//                this.parent = parent;
//                this.id = this.newId();
//                this.unsafe = this.newUnsafe();
//                this.pipeline = this.newChannelPipeline();
//            }

//            总结：
//            https://www.jianshu.com/writer#/notebooks/40151609/notes/54737658/preview
            ChannelFuture f = b.bind(8888).sync();

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}