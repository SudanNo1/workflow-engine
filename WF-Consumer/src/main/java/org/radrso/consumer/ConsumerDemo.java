package org.radrso.consumer;

import org.radrso.demo.HelloWorld;
import org.radrso.utils.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;

/**
 * Created by raomengnan on 16-12-2.
 */
public class ConsumerDemo {

    public static void main(String[] args) {
        BeanFactoryUtils.init();
        HelloWorld helloWorld = (HelloWorld) BeanFactoryUtils.getContext().getBean("helloWorldImpl");
        System.out.println(helloWorld.say("HHHHHH"));
    }
}
