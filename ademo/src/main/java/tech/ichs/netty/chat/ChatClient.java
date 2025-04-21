package tech.ichs.netty.chat;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.util.Scanner;

public class ChatClient {

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //pipeline.addLast(new DelimiterBasedFrameDecoder(1024, Unpooled.copiedBuffer("_"
                            // .getBytes())));
//                            pipeline.addLast("decoder", new StringDecoder());
//                            pipeline.addLast("encoder", new StringEncoder());
                            //加入自己的业务处理handler
                            pipeline.addLast(new ClientInboundHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 9000).sync();
            //得到 channel
            Channel channel = channelFuture.channel();
            System.out.println("========" + channel.localAddress() + "========");
            //客户端需要输入信息， 创建一个扫描器
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
                //通过 channel 发送到服务器端
//                channel.writeAndFlush(msg.getBytes(StandardCharsets.UTF_8));
                ByteBuf buf = Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
                channel.writeAndFlush(buf);
            }
            /*for (int i = 0; i < 200; i++) {
                channel.writeAndFlush("hello，诸葛!" + "_");
            }*/
        } finally {
            group.shutdownGracefully();
        }
    }
}
class ClientInboundHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        System.out.println(((ByteBuf) msg).toString(CharsetUtil.UTF_8));
    }
/*
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String msg;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while ((msg = br.readLine()) != null) {
            ctx.channel().writeAndFlush(msg);
        }
    }*/
}