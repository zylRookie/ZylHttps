package com.tc.zyl.zylhttpmanager;

import java.util.ArrayList;

/**
 * Created by Administrator on 2017/10/8 0008.
 */

public class GoodEvaluteModel {
    public String msgcode;
    public String msg;
    public ArrayList<DataBean> data;
    public static class DataBean {
        public String id;
        public String pao_name;
        public String Evaluate;
        public String Pao_Image;
    }
}
