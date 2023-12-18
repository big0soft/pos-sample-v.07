package com.nexgo.apiv3demo;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by blatand on 2022-3-18 13:48
 * 一个专门的集合类对所有的活动（ Activity）进行管理
 *  *
 */
public class ActivityCollector {

    public static List<Activity> activities = new ArrayList<Activity>();

    /**
     * 添加Activity
     * @param activity 添加的Activity对象
     * */
    public static void addActivity(Activity activity) {
        activities.add(activity);
    }

    /**
     * 删除Activity
     * @param activity 删除的Activity对象
     * */
    public static void removeActivity(Activity activity) {
        activities.remove(activity);
    }

    public static void finishSpecialActivity(String activityName){
        //在activities集合中找到类名与指定类名相同的Activity就关闭
        for (Activity activity : activities){
            String name= activity.getClass().getName();//activity的包名+类名
            if(name.equals(activityName)){
                if(activity.isFinishing()){
                    //当前activity如果已经Finish，则只从activities清除就好了
                    activities.remove(activity);
                } else {
                    //没有Finish则Finish
                    activity.finish();
                }
            }
        }
    }
    public static boolean isExist(String activityName){
        //在activities集合中找到类名与指定类名相同的Activity就关闭
        for (Activity activity : activities){
            String name= activity.getClass().getName();//activity的包名+类名
            if(name.equals(activityName)){
                return true;
            }else{
                return false;
            }
        }
        return false;
    }
    public static void finishAllActivity(){
        if(activities.size() == 0)return;
        //在activities集合中找到类名与指定类名相同的Activity就关闭
        for (Activity activity : activities){
//            String name= activity.getClass().getName();//activity的包名+类名

            if(activity.isFinishing()){
                //当前activity如果已经Finish，则只从activities清除就好了
                activities.remove(activity);
            } else {
                //没有Finish则Finish
                activity.finish();
            }

        }
    }
}
