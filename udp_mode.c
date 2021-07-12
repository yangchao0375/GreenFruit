#include <stdio.h>
#include <unistd.h>

#include "../include/ap_mode.h"
#include "../include/car_mode.h"
#include "../include/sta_mode.h"
#include "../include/trace_mode.h"
#include "cJSON.h"
#include "cmsis_os2.h"
#include "hi_wifi_api.h"
#include "lwip/ip_addr.h"
#include "lwip/netifapi.h"
#include "lwip/sockets.h"
#include "ohos_init.h"

static char recvcon[1024];
static char recvline[1024];
static char ssid[50];
static char passwd[50];

char *phoneIp;

void udp_sta_thread(void *pdata) {
    printf("udp_sta_thread...\r\n");
    (void)pdata;
    int ret;
    struct sockaddr_in servaddr;
    cJSON *recvjson;
    int sockfd = socket(PF_INET, SOCK_DGRAM, 0);
    //服务器 ip port
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(28881);
    bind(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr));
    while (1) {
        struct sockaddr_in addrClient;
        int sizeClientAddr = sizeof(struct sockaddr_in);
        memset(recvcon, sizeof(recvcon), 0);
        ret = recvfrom(sockfd, recvcon, 1024, 0, (struct sockaddr *)&addrClient, (socklen_t *)&sizeClientAddr);
        if (ret > 0) {
            char *pClientIP = inet_ntoa(addrClient.sin_addr);
            printf("%s-%d(%d) says:%s\n", pClientIP, ntohs(addrClient.sin_port), addrClient.sin_port, recvcon);
            //进行json解析
            recvjson = cJSON_Parse(recvcon);
            printf("ssid : %s\r\n", cJSON_GetObjectItem(recvjson, "ssid")->valuestring);
            printf("passwd : %s\r\n", cJSON_GetObjectItem(recvjson, "passwd")->valuestring);
            memset(ssid, sizeof(ssid), 0);
            memset(passwd, sizeof(passwd), 0);
            strcpy(ssid, cJSON_GetObjectItem(recvjson, "ssid")->valuestring);
            strcpy(passwd, cJSON_GetObjectItem(recvjson, "passwd")->valuestring);
            cJSON_Delete(recvjson);
            //先停止AP模式
            wifi_stop_softap();
            //启动STA模式
            start_sta_connect(ssid, passwd);
        }
    }
}

void start_udp_sta_thread(void) {
    printf("start_udp_sta_thread...\r\n");
    osThreadAttr_t attr;
    attr.name = "udp_thread";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 10240;
    attr.priority = 36;

    if (osThreadNew((osThreadFunc_t)udp_sta_thread, NULL, &attr) == NULL) {
        printf("start_udp_sta_thread....%s----%d\r\n", __FILE__, __LINE__);
    }
}

void udp_ctrl_car_thread(void *pdata) {
    printf("udp_ctrl_car_thread...\r\n");
    trace_start_func();
    (void)pdata;
    int ret;
    struct sockaddr_in servaddr;
    cJSON *recvjson;
    int sockfd = socket(PF_INET, SOCK_DGRAM, 0);
    //服务器 ip port
    bzero(&servaddr, sizeof(servaddr));
    servaddr.sin_family = AF_INET;
    servaddr.sin_addr.s_addr = htonl(INADDR_ANY);
    servaddr.sin_port = htons(28883);
    bind(sockfd, (struct sockaddr *)&servaddr, sizeof(servaddr));

    while (1) {
        struct sockaddr_in addrClient;
        int sizeClientAddr = sizeof(struct sockaddr_in);

        memset(recvline, sizeof(recvline), 0);
        ret = recvfrom(sockfd, recvline, 1024, 0, (struct sockaddr *)&addrClient, (socklen_t *)&sizeClientAddr);

        if (ret > 0) {
            char *pClientIP = inet_ntoa(addrClient.sin_addr);

            printf("%s-%d(%d) says:%s\n", pClientIP, ntohs(addrClient.sin_port), addrClient.sin_port, recvline);

            //进行json解析
            recvjson = cJSON_Parse(recvline);

            if (recvjson != NULL) {
                if (cJSON_GetObjectItem(recvjson, "cmd")->valuestring != NULL) {
                    printf("cmd : %s\r\n", cJSON_GetObjectItem(recvjson, "cmd")->valuestring);
                    if (strcmp("forward", cJSON_GetObjectItem(recvjson, "cmd")->valuestring) == 0) {
                        set_car_status(CAR_STATUS_FORWARD);
                        printf("forward\r\n");
                    } else if (strcmp("backward", cJSON_GetObjectItem(recvjson, "cmd")->valuestring) == 0) {
                        set_car_status(CAR_STATUS_BACKWARD);
                        printf("backward\r\n");
                    } else if (strcmp("left", cJSON_GetObjectItem(recvjson, "cmd")->valuestring) == 0) {
                        set_car_status(CAR_STATUS_LEFT);
                        printf("left\r\n");
                    } else if (strcmp("right", cJSON_GetObjectItem(recvjson, "cmd")->valuestring) == 0) {
                        set_car_status(CAR_STATUS_RIGHT);
                        printf("right\r\n");
                    } else if (strcmp("stop", cJSON_GetObjectItem(recvjson, "cmd")->valuestring) == 0) {
                        set_car_status(CAR_STATUS_STOP);
                        printf("stop\r\n");
                    } else if (strcmp("speedup", cJSON_GetObjectItem(recvjson, "cmd")->valuestring) == 0) {
                        speedup();
                        printf("speedup\r\n");
                    } else if (strcmp("speeddown", cJSON_GetObjectItem(recvjson, "cmd")->valuestring) == 0) {
                        speeddown();
                        printf("speeddown\r\n");
                    } else {
                        phoneIp = cJSON_GetObjectItem(recvjson, "cmd")->valuestring;
                        printf("phoneIP : %s\r\n", phoneIp);
                        trace_start_func();
                    }
                }
                cJSON_Delete(recvjson);
            }
        }
    }
}

void start_udp_ctrl_car_thread(void) {
    printf("start_udp_ctrl_car_thread...\r\n");
    osThreadAttr_t attr;
    attr.name = "udp_ctrl_car_thread";
    attr.attr_bits = 0U;
    attr.cb_mem = NULL;
    attr.cb_size = 0U;
    attr.stack_mem = NULL;
    attr.stack_size = 10240;
    attr.priority = 36;

    if (osThreadNew((osThreadFunc_t)udp_ctrl_car_thread, NULL, &attr) == NULL) {
        printf("start_udp_ctrl_car_thread....%s----%d\r\n", __FILE__, __LINE__);
    }
}