package utils;
import android.app.Application;
import android.content.Context;
import com.danikula.videocache.HttpProxyCacheServer;

public class CacheUtils extends Application {
    private static CacheUtils cacheUtils;
    private HttpProxyCacheServer proxy;
    private CacheUtils(Context context) {
         cacheUtils = CacheUtils.this;
         proxy = new HttpProxyCacheServer(context);
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        if (cacheUtils == null)
            new CacheUtils(context);
        return cacheUtils.proxy;
    }
}
