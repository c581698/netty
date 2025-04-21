package tech.ichs.netty.chat;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;

public class ChatServer {
    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(8);
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //加入特殊分隔符分包解码器
                            //pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("_"
                            // .getBytes())));
//                            pipeline.addLast("decoder", new StringDecoder());
//                            pipeline.addLast("encoder", new StringEncoder());
                            //加入自己的业务处理handler
                            pipeline.addLast(new ServerInboundHandler());
                        }
                    });
            System.out.println("聊天室server启动。。");
            ChannelFuture channelFuture = bootstrap.bind(9000).sync();
            //关闭通道
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
class ServerInboundHandler extends ChannelInboundHandlerAdapter {
    static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        channelGroup.writeAndFlush(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        channelGroup.add(ctx.channel());
    }
}
class ServerSocketChannelInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {

        System.out.println("registered");
    }
}