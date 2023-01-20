public class JSWrapper {

    private static final String TAG = "JSWrapper";


    public interface ScriptLoader {
        String load(String name);
    }

    static List<ScriptLoader> loaders = new ArrayList<>();
    static QuickJSContext jsContext;
    static Context andContext = null;

    public static void addLoader(ScriptLoader loader) {
        if (loader == null || loaders.contains(loader))
            return;
        loaders.add(loader);
    }

    public static void init(Context context) {
        andContext = context;
        QuickJSLoader.init();
        JSModule.setModuleLoader(new JSModule.Loader() {
            @Override
            public String getModuleScript(String moduleName) {
                for (ScriptLoader loader : loaders) {
                    String tryLoad = loader.load(moduleName);
                    if (tryLoad != null)
                        return tryLoad;
                }
                return "";
            }
        });
        jsContext = QuickJSContext.create();
        initConsole();
        initOkHttp();
        initLocalStorage();
    }

    static void initConsole() {
        jsContext.evaluate("var console = {};");
        JSObject console = (JSObject) jsContext.getGlobalObject().getProperty("console");
        console.setProperty("log", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                StringBuilder b = new StringBuilder();
                for (Object o : args) {
                    b.append(o == null ? "null" : o.toString());
                }
                System.out.println(TAG + " >>> " + b.toString());
                return null;
            }
        });
    }

    static void initLocalStorage() {
        SharedPreferences sharedPreferences = andContext.getSharedPreferences("js_engine", Context.MODE_PRIVATE);
        jsContext.evaluate("var local = {};");
        JSObject console = (JSObject) jsContext.getGlobalObject().getProperty("local");
        console.setProperty("get", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                return sharedPreferences.getString(args[0].toString() + "_" + args[1].toString(), "");
            }
        });
        console.setProperty("set", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                sharedPreferences.edit().putString(args[0].toString() + "_" + args[1].toString(), args[2].toString()).commit();
                return null;
            }
        });
    }
static void initOkHttp() {
        jsContext.getGlobalObject().setProperty("req", new JSCallFunction() {
            @Override
            public Object call(Object... args) {
                try {
                    String url = args[0].toString();
                    JSONObject opt = new JSONObject(jsContext.stringify((JSObject) args[1]));
                    Headers.Builder headerBuilder = new Headers.Builder();
                    JSONObject optHeader = opt.optJSONObject("headers");
                    if (optHeader != null) {
                        Iterator<String> hdKeys = optHeader.keys();
                        while (hdKeys.hasNext()) {
                            String k = hdKeys.next();
                            String v = optHeader.optString(k);
                            headerBuilder.add(k, v);
                        }
                    }
                    Headers headers = headerBuilder.build();
                    String method = opt.optString("method").toLowerCase();
                    Request.Builder requestBuilder = new Request.Builder().url(url).headers(headers);
                    Request request = null;
                    if (method.equals("post")) {
                        RequestBody body = RequestBody.create(MediaType.get(headers.get("content-type")), opt.optString("body", ""));
                        request = requestBuilder.post(body).build();
                    } else if (method.equals("header")) {
                        request = requestBuilder.head().build();
                    } else {
                        request = requestBuilder.get().build();
                    }
                    Response response = opt.optInt("redirect", 1) != 1 ? OkHttpUtil.defaultClient().newCall(request).execute() : OkHttpUtil.noRedirectClient().newCall(request).execute();
                    JSObject jsObject = jsContext.createNewJSObject();
                    Set<String> resHeaders = response.headers().names();
                    JSObject resHeader = jsContext.createNewJSObject();
                    for (String header : resHeaders) {
                        resHeader.setProperty(header, response.header(header));
                    }
                    jsObject.setProperty("headers", resHeader);
                    if (opt.optInt("buffer", 0) == 1) {
                        JSArray array = jsContext.createNewJSArray();
                        byte[] bytes = response.body().bytes();
                        for (int i = 0; i < bytes.length; i++) {
                            array.set(bytes[i], i);
                        }
                        jsObject.setProperty("content", array);
                    } else {
                        jsObject.setProperty("content", response.body().string());
                    }
                    return jsObject;
                } catch (Throwable throwable) {
                }
                return "";
            }
        });
    }

    public static JSObject loadSpider(String key, String api, String ext) {
        AtomicReference<JSObject> result = new AtomicReference<>(null);
        String script = "";
        for (ScriptLoader loader : loaders) {
            String tryLoad = loader.load(api);
            if (tryLoad != null) {
                script = tryLoad;
                break;
            }
        }
        String jsObjKey = "__JS_SPIDER__" + key.trim() + "__";
        script = script.replaceFirst("__JS_SPIDER__", "globalThis." + jsObjKey);
        String finalScript = script;
        jsContext.evaluateModule(finalScript);
        JSObject jsObject = (JSObject) jsContext.getProperty(jsContext.getGlobalObject(), jsObjKey);
        jsObject.getJSFunction("init").call(ext);
        System.out.println(jsObject.getJSFunction("home").call(true));
        System.out.println(jsObject.getJSFunction("homeVod").call());
        System.out.println(jsObject.getJSFunction("category").call("1", 1, "", ""));
        System.out.println(jsObject.getJSFunction("detail").call(136405));
        System.out.println(jsObject.getJSFunction("play").call("136405", "https://a.zhaojiuwanwu.top/api/GetDownUrlMu/33ecf636f7c2429c8d9c2b10defdb26a/16f682cd10134ecda540557cd79e5a30.m3u8?sign=4f5ac17015f65e1aa7247fd2035dc9b5&t=1663942319", jsContext.createNewJSArray()));
        System.out.println(jsObject.getJSFunction("search").call("请回答1997"));
        result.set(jsObject);
        return result.get();
    }
}