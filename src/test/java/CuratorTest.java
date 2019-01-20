import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * Curator使用例子
 * @author zhi.li
 * @Description
 * @created 2019/1/8 20:51
 */
public class CuratorTest {
    private static RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);

    // 服务器列表，格式host1:port1,host2:port2
    private static CuratorFramework client = CuratorFrameworkFactory.builder()
            .connectString("127.0.0.1:2181")
            .sessionTimeoutMs(3000)
            .connectionTimeoutMs(5000)
            .retryPolicy(retryPolicy)
            .build();
    public static void main(String[] args) throws Exception{

        // 创建会话
        client.start();

        // region 同步创建节点
        // 创建一个初始内容为空的节点
        Stat checkStat = client.checkExists().forPath("/test");
        if(checkStat == null) {
            client.create().forPath("/test");
        }

        //创建一个初始内容不为空的节点
        checkStat = client.checkExists().forPath("/test1");
        if(checkStat == null) {
            client.create().forPath("/test1", "hello".getBytes());
        }

        //创建一个初始内容为空的临时节点
        client.create().withMode(CreateMode.EPHEMERAL).forPath("/test/test1");

        //创建一个初始内容不为空的临时节点，可以实现递归创建
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL)
                .forPath("/test/test2","test2".getBytes());
        // endregion

        // region 异步创建节点
        client.create().withMode(CreateMode.EPHEMERAL)
                .inBackground((client, event) -> System.out.println("当前线程：" + Thread.currentThread().getName() + ",code:"
                + event.getResultCode() + ",type:" + event.getType())).forPath("/async-test1");
        // endregion

        // region 获取节点内容
        byte[] data = client.getData().forPath("/test1");
        System.out.println(new String(data));
        //传入一个旧的stat变量,来存储服务端返回的最新的节点状态信息
        Stat nodeStat = new Stat();
        byte[] data2 = client.getData().storingStatIn(nodeStat).forPath("/test1");
        System.out.println(new String(data2));
        System.out.println("node stat: " +nodeStat);
        // endregion


        // region 更新数据
        Stat stat = client.setData().forPath("/test1");
        // 指定版本更新
        client.setData().withVersion(stat.getVersion()).forPath("/test1", "helloworld".getBytes());
        byte[] updateData = client.getData().forPath("/test1");
        System.out.println("after update stat: " +stat);
        System.out.println("after update: " +new String(updateData));
        // endregion

        // region 删除节点
        //只能删除叶子节点

        checkStat = client.checkExists().forPath("/test1");
        if(checkStat != null) {
            System.out.println("删除前test节点存在");
        }
        // 父目录下有子节点,不能直接删除,会报错: KeeperErrorCode = Directory not empty for
        client.delete().forPath("/test1");
        checkStat = client.checkExists().forPath("/test1");
        if(checkStat == null) {
            System.out.println("删除后test节点不存在");
        }

        List<String> childs = client.getChildren().forPath("/test");
        if(!childs.isEmpty()){
            System.out.println("删除前test节点下有子节点");
        }

        //删除一个节点,并递归删除其所有子节点
        client.delete().deletingChildrenIfNeeded().forPath("/test");
        checkStat = client.checkExists().forPath("/test");
        if(checkStat == null){
            System.out.println("删除后test节点下没有子节点");
        }
        // 强制指定版本进行删除
        if(checkStat != null) {
            client.delete().withVersion(1).forPath("/test");
        }
        //注意:由于一些网络原因,上述的删除操作有可能失败,使用guaranteed(),
        // 如果删除失败,会记录下来,只要会话有效,就会不断的重试,直到删除成功为止
        if(checkStat != null) {
            client.delete().guaranteed().forPath("/test");
        }
        // endregion
    }
}
