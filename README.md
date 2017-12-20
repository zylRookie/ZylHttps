# ZylHttps
封装okhttp 封装了get，post，postjson，postFile等方法链式调用

使用：
在根目录上build加上

allprojects {
		repositories {
			...
			maven { url 'https://www.jitpack.io' }
		}
	}
  
  
  在app项目中build加上
	dependencies {
	        compile 'com.github.zylRookie:ZylHttps:1.0.2'
	}
  
  /*
  *objet 实体类Bean
  */
 HttpManager.get().url(url)
     .build().execute(new HttpManager.ResponseCallback<Object>() {
            @Override
            public void onError() {

            }

            @Override
            public void onSuccess(Object o) {

            }
        });
