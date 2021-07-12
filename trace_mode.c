#include <arpa/inet.h>
#include <hi_early_debug.h>
#include <hi_gpio.h>
#include <hi_io.h>
#include <hi_task.h>
#include <hi_types_base.h>
#include <netinet/in.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <sys/socket.h>
#include <sys/types.h>
#include <unistd.h>

#include "../include/oled_ssd1306.h"
#include "cmsis_os2.h"
#include "hi_wifi_api.h"
#include "lwip/ip_addr.h"
#include "lwip/netifapi.h"
#include "lwip/sockets.h"
#include "ohos_init.h"

extern char *phoneIp;

void trace_get_func(const char *arg) {
    printf("trace_get_func.....\r\n");
    static char textip[128] = {0};
    snprintf(textip, sizeof(textip), "%s", phoneIp);
    (void)arg;
    hi_gpio_value io_value11 = 0;
    hi_gpio_value io_value12 = 0;
    int fd = socket(AF_INET, SOCK_DGRAM, 0);  // AF_INET和SOCK_DGRAM的组合对应UDP协议
    struct sockaddr_in address;
    char line[100] = "28883";
    bzero(&address, sizeof(address));
    address.sin_family = AF_INET;
    address.sin_addr.s_addr = inet_addr(textip);
    address.sin_port = htons(3000);
    while (1) {
        usleep(200000);
        hi_gpio_get_input_val(HI_GPIO_IDX_11, &io_value11);
        hi_gpio_get_input_val(HI_GPIO_IDX_12, &io_value12);
        if ((io_value11 == HI_GPIO_VALUE0) | (io_value12 == HI_GPIO_VALUE0)) {
            printf("trace..black.. %d----phoneIp: %s \r\n", 0, textip);
            //发送信息到手机
            sendto(fd, line, strlen(line), 0, (struct sockaddr *)&address, sizeof(address));
            printf("send string success\n");
        }
    }
}

void trace_start_func(void) {
    //开启另一个线程，获取寻迹模块数据
    osThreadAttr_t trace_attr;
    trace_attr.name = "trace_get_func";
    trace_attr.attr_bits = 0U;  //无符号整型 0
    trace_attr.cb_mem = NULL;
    trace_attr.cb_size = 0U;
    trace_attr.stack_mem = NULL;
    trace_attr.stack_size = 10240;
    trace_attr.priority = 36;

    if (osThreadNew((osThreadFunc_t)trace_get_func, NULL, &trace_attr) == NULL) {
        printf("FILE: %d,  LINE: %d, %s\r\n", __FILE__, __LINE__, "trace_get_func error");
    }
}