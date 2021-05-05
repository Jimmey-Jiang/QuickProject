package com.project.QuickProject.monitor.custom;

/**
 * User
 *
 * @author Jimmey-Jiang
 * @date 2020/6/25
 */
public class User implements UserMBean {

    @Override
    public String getName() {
        return "风筝";
    }

    @Override
    public String getPassword() {
        return "密码不可见";
    }

    @Override
    public String getPhone() {
        return "18900000000";
    }

    @Override
    public void say() {
        System.out.println("Hello JMX");
    }
}
