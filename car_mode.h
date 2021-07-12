#ifndef __CAR_MODE_H__
#define __CAR_MODE_H__

typedef enum {
    /*停止*/
    CAR_STATUS_STOP,

    /*前进*/
    CAR_STATUS_FORWARD,

    /*后退*/
    CAR_STATUS_BACKWARD,

    /*左转*/
    CAR_STATUS_LEFT,

    /*右转*/
    CAR_STATUS_RIGHT,

    /** Maximum value */
    CAR_STATUS_MAX
} CarStatus;

struct car_sys_info {
    CarStatus volatile cur_status;
    CarStatus volatile go_status;
    char volatile status_change;  //状态是否有改变的标记
};

void car_run(void);

void set_car_status(CarStatus status);

void speedup(void);

void speeddown(void);

#endif