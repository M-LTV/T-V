- 猫影视学生版TVBOX 兼容下列APP
- 支持tvbox&各类魔改版
- 站源直播配置文件
- 历史猫影视配置
- 此配置多jar爬虫(仅支持猫影视学生版)
-
壁纸:
```javascript
{
    "wallpaper": "壁纸路径" //例子 "https://picsum.photos/1920/1080/?blur=10"
}
```

单Jar的定义方式不变，直接放路径即可:
```javascript
{
    ...
    "spider": "jar地址"
    ...
```

多Jars:
```javascript
{
    ...
    "spider": [
        { "n": "default", "v": "jar地址" }, //默认jar
        { "n": "jar1", "v": "jar1 地址" },
        { "n": "jar2", "v": "jar2 地址" }
        ...
    ],
    "sites": [
        ...
        { "key": "csp_csp1", "name": "CSP1", ..., "spider": "jar1" }, //对应spider里的n值
        { "key": "csp_csp2", "name": "CSP2", ..., "spider": "jar2" },
        { "key": "csp_csp3", "name": "CSP3", ... },  //没有spider参数的话，使用默认jar
        { "key": "csp_csp4", "name": "CSP4", ..., "spider": "jar2" },
        ...
    ],
    ...
}
```
