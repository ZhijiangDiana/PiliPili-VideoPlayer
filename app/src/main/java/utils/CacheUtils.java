package utils;
import android.app.Application;
import android.content.Context;
import com.danikula.videocache.HttpProxyCacheServer;

public class CacheUtils extends Application {
    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        CacheUtils app = (CacheUtils) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }
}
