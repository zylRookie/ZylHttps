# ZylHttps
封装okhttp 封装了get，post，postjson，postFile等方法链式调用

### 1、在自己项目中添加本项目依赖：

    compile 'com.github.zylRookie:ZylHttps:1.0.2'

### 2、在根目录中添加：

    allprojects {
        repositories {
           ...
          maven {
              url "https://jitpack.io"
          }
       }
    }
  
 ### 3、在项目中使用：

    //object 是你使用的实体类Bean（换成你自己的）
    HttpManager.get().url(url)
                .build().execute(new HttpManager . ResponseCallback < Object >() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(Object o) {

            }
        });
