#include <hi_early_debug.h>
#include <hi_gpio.h>
#include <hi_io.h>
#include <hi_task.h>
#include <hi_types_base.h>
#include <stdio.h>
#include <unistd.h>

#include "./include/ap_mode.h"
#include "./include/oled_ssd1306.h"
#include "cmsis_os2.h"
#include "hi_wifi_api.h"
#include "lwip/ip_addr.h"
#include "lwip/netifapi.h"
#include "lwip/sockets.h"
#include "ohos_init.h"

volatile char start_wifi_config_flag = 0;
hi_void gpio5_user_btn_func(hi_void *arg) {
    hi_unref_param(arg);
    //启动ap
    start_wifi_config_flag = 1;
}

hi_void gpio5_user_btn_startap(hi_void) {
    hi_u32 ret;
    hi_gpio_init();
    hi_io_set_func(HI_IO_NAME_GPIO_5, HI_IO_FUNC_GPIO_5_GPIO);
    ret = hi_gpio_set_dir(HI_GPIO_IDX_5, HI_GPIO_DIR_IN);
    if (ret != HI_ERR_SUCCESS) {
        printf("FILE: %d,  LINE: %d, %s\r\n", __FILE__, __LINE__, "hi_gpio_set_dir error");
        return;
    }
    //注册user按钮回调函数
    ret = hi_gpio_register_isr_function(HI_GPIO_IDX_5, HI_INT_TYPE_EDGE, HI_GPIO_EDGE_RISE_LEVEL_HIGH, gpio5_user_btn_func, HI_NULL);
}

void *wifi_config_func(const char *arg) {
    printf("wifi_config_func...\r\n");
    OledInit();
    arg = arg;
    //点击user按钮启动ap
    gpio5_user_btn_startap();
    while (start_wifi_config_flag == 0) {
        usleep(300000);
    }
    printf("FILE: %d,  LINE: %d, %s\r\n", __FILE__, __LINE__, "ap starting.....\r\n");
    //调用ap_mode里的wifi_start_ap()启动ap
    wifi_start_ap();
    osThreadExit();
    return NULL;
}

void wifi_config_start(void) {
    printf("wifi_config_start.....\r\n");

    osThreadAttr_t attr;
    attr.name = "wifi_config_func";
    attr.attr_bits = 0U;  //无符号整型 0
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 10240;
    attr.priority = 36;

    if (osThreadNew((osThreadFunc_t)wifi_config_func, NULL, &attr) == NULL) {
        printf("FILE: %d,  LINE: %d, %s\r\n", __FILE__, __LINE__, "wifi_config_start error");
    }
}
SYS_RUN(wifi_config_start);