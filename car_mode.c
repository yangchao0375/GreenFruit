#include "../include/car_mode.h"

#include <stdio.h>
#include <unistd.h>

#include "../include/udp_mode.h"
#include "cmsis_os2.h"
#include "hi_wifi_api.h"
#include "ohos_init.h"
#include "wifiiot_gpio.h"
#include "wifiiot_gpio_ex.h"
#include "wifiiot_pwm.h"

struct car_sys_info car_info;
volatile char start_wifi_connected_flg = 0;  //多线程可见

static long duty = 8900;
static long freq = 10000;

void car_info_init(void) {
    car_info.go_status = CAR_STATUS_STOP;
    car_info.cur_status = CAR_STATUS_STOP;
}

void set_car_status(CarStatus status) {
    if (status != car_info.cur_status) {
        car_info.status_change = 1;  // 1:状态有改变，0：表示状态没
    }
    car_info.go_status = status;  //传过来的状态设置为go_status
}

void pwm_init(void) {
    GpioInit();
    //引脚复用
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_0, WIFI_IOT_IO_FUNC_GPIO_0_PWM3_OUT);
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_1, WIFI_IOT_IO_FUNC_GPIO_1_PWM4_OUT);
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_9, WIFI_IOT_IO_FUNC_GPIO_9_PWM0_OUT);
    IoSetFunc(WIFI_IOT_IO_NAME_GPIO_10, WIFI_IOT_IO_FUNC_GPIO_10_PWM1_OUT);

    //初始化pwm
    PwmInit(WIFI_IOT_PWM_PORT_PWM3);
    PwmInit(WIFI_IOT_PWM_PORT_PWM4);
    PwmInit(WIFI_IOT_PWM_PORT_PWM0);
    PwmInit(WIFI_IOT_PWM_PORT_PWM1);
}

//加速
void speedup(void) {
    //先停止PWM
    PwmStop(WIFI_IOT_PWM_PORT_PWM3);
    PwmStop(WIFI_IOT_PWM_PORT_PWM4);
    PwmStop(WIFI_IOT_PWM_PORT_PWM0);
    PwmStop(WIFI_IOT_PWM_PORT_PWM1);

    duty = 9980;
    //启动A路PWM
    PwmStart(WIFI_IOT_PWM_PORT_PWM3, duty, freq);
    PwmStart(WIFI_IOT_PWM_PORT_PWM0, duty, freq);
}

//减速
void speeddown(void) {
    //先停止PWM
    PwmStop(WIFI_IOT_PWM_PORT_PWM3);
    PwmStop(WIFI_IOT_PWM_PORT_PWM4);
    PwmStop(WIFI_IOT_PWM_PORT_PWM0);
    PwmStop(WIFI_IOT_PWM_PORT_PWM1);

    duty = 8900;
    //启动A路PWM
    PwmStart(WIFI_IOT_PWM_PORT_PWM3, duty, freq);
    PwmStart(WIFI_IOT_PWM_PORT_PWM0, duty, freq);
}

//前进
void pwm_forward(void) {
    //先停止PWM
    PwmStop(WIFI_IOT_PWM_PORT_PWM3);
    PwmStop(WIFI_IOT_PWM_PORT_PWM4);
    PwmStop(WIFI_IOT_PWM_PORT_PWM0);
    PwmStop(WIFI_IOT_PWM_PORT_PWM1);

    //启动A路PWM
    PwmStart(WIFI_IOT_PWM_PORT_PWM3, duty, freq);
    PwmStart(WIFI_IOT_PWM_PORT_PWM0, duty, freq);
}
void car_forward(void) {
    if (car_info.go_status != CAR_STATUS_FORWARD) {
        //直接退出
        return;
    }
    if (car_info.cur_status == car_info.go_status) {
        //状态没有变化，直接推出
        return;
    }

    car_info.cur_status = car_info.go_status;
    printf("pwm_forward \r\n");
    pwm_forward();
}

//后退
void pwm_backward(void) {
    //先停止PWM
    PwmStop(WIFI_IOT_PWM_PORT_PWM3);
    PwmStop(WIFI_IOT_PWM_PORT_PWM4);
    PwmStop(WIFI_IOT_PWM_PORT_PWM0);
    PwmStop(WIFI_IOT_PWM_PORT_PWM1);

    //启动A路PWM
    PwmStart(WIFI_IOT_PWM_PORT_PWM4, duty, freq);
    PwmStart(WIFI_IOT_PWM_PORT_PWM1, duty, freq);
}
void car_backward(void) {
    if (car_info.go_status != CAR_STATUS_BACKWARD) {
        //直接退出
        return;
    }
    if (car_info.cur_status == car_info.go_status) {
        //状态没有变化，直接推出
        return;
    }

    car_info.cur_status = car_info.go_status;
    printf("pwm_backward \r\n");
    pwm_backward();
}

//左转
void pwm_left(void) {
    //先停止PWM
    PwmStop(WIFI_IOT_PWM_PORT_PWM3);
    PwmStop(WIFI_IOT_PWM_PORT_PWM4);
    PwmStop(WIFI_IOT_PWM_PORT_PWM0);
    PwmStop(WIFI_IOT_PWM_PORT_PWM1);

    //启动A路PWM
    PwmStart(WIFI_IOT_PWM_PORT_PWM0, duty, freq);
}
void car_left(void) {
    if (car_info.go_status != CAR_STATUS_LEFT) {
        //直接退出
        return;
    }
    if (car_info.cur_status == car_info.go_status) {
        //状态没有变化，直接推出
        return;
    }

    car_info.cur_status = car_info.go_status;
    printf("pwm_left \r\n");
    pwm_left();
}

//右转
void pwm_right(void) {
    //先停止PWM
    PwmStop(WIFI_IOT_PWM_PORT_PWM3);
    PwmStop(WIFI_IOT_PWM_PORT_PWM4);
    PwmStop(WIFI_IOT_PWM_PORT_PWM0);
    PwmStop(WIFI_IOT_PWM_PORT_PWM1);

    //启动A路PWM
    PwmStart(WIFI_IOT_PWM_PORT_PWM3, duty, freq);
}
void car_right(void) {
    if (car_info.go_status != CAR_STATUS_RIGHT) {
        //直接退出
        return;
    }
    if (car_info.cur_status == car_info.go_status) {
        //状态没有变化，直接推出
        return;
    }

    car_info.cur_status = car_info.go_status;
    printf("pwm_right \r\n");
    pwm_right();
}

//停止
void pwm_stop(void) {
    //停止PWM
    PwmStop(WIFI_IOT_PWM_PORT_PWM3);
    PwmStop(WIFI_IOT_PWM_PORT_PWM4);
    PwmStop(WIFI_IOT_PWM_PORT_PWM0);
    PwmStop(WIFI_IOT_PWM_PORT_PWM1);
}
void car_stop(void) {
    car_info.cur_status = car_info.go_status;
    printf("pwm_stop \r\n");
    pwm_stop();
}

void car_run(void) {
    start_udp_ctrl_car_thread();
    pwm_init();
    car_info_init();
    // set_car_status(CAR_STATUS_FORWARD);

    while (1) {
        if (car_info.status_change) {
            car_info.status_change = 0;

            switch (car_info.go_status) {
                case CAR_STATUS_STOP:
                    car_stop();
                    break;

                case CAR_STATUS_FORWARD:
                    car_forward();
                    break;

                case CAR_STATUS_BACKWARD:
                    car_backward();
                    break;

                case CAR_STATUS_LEFT:
                    car_left();
                    break;

                case CAR_STATUS_RIGHT:
                    car_right();
                    break;

                default:

                    break;
            }
        }
        usleep(1000);
    }
}
