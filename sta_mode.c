#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "../include/car_mode.h"
#include "../include/oled_ssd1306.h"
#include "../include/udp_mode.h"
#include "cmsis_os2.h"
#include "hi_wifi_api.h"
#include "lwip/api_shell.h"
#include "lwip/ip_addr.h"
#include "lwip/netifapi.h"
#include "lwip/sockets.h"
#include "ohos_init.h"
#include "string.h"
#include "wifi_device.h"
#include "wifiiot_adc.h"
#include "wifiiot_gpio.h"
#include "wifiiot_gpio_ex.h"
#include "wifiiot_pwm.h"

static char *SecurityTypeName(WifiSecurityType type) {
    switch (type) {
        case WIFI_SEC_TYPE_OPEN:
            return "OPEN";
        case WIFI_SEC_TYPE_WEP:
            return "WEP";
        case WIFI_SEC_TYPE_PSK:
            return "PSK";
        case WIFI_SEC_TYPE_SAE:
            return "SAE";
        default:
            break;
    }
    return "unkow";
}

static void PrintLinkedInfo(WifiLinkedInfo *info) {
    if (!info) return;

    static char macAddress[32] = {0};
    unsigned char *mac = info->bssid;
    snprintf(macAddress, sizeof(macAddress), "%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
    printf("bssid: %s, rssi: %d, connState: %d, reason: %d, ssid: %s\r\n", macAddress, info->rssi, info->connState, info->disconnectedReason, info->ssid);
}

void PrintScanResult(void) {
    WifiScanInfo scanResult[WIFI_SCAN_HOTSPOT_LIMIT] = {0};
    uint32_t resultSize = WIFI_SCAN_HOTSPOT_LIMIT;

    memset(&scanResult, 0, sizeof(scanResult));
    WifiErrorCode errCode = GetScanInfoList(scanResult, &resultSize);
    if (errCode != WIFI_SUCCESS) {
        printf("GetScanInfoList failed: %d\r\n", errCode);
    }
    for (uint32_t i = 0; i < resultSize; i++) {
        static char macAddress[32] = {0};
        WifiScanInfo info = scanResult[i];
        unsigned char *mac = info.bssid;
        snprintf(macAddress, sizeof(macAddress), "%02X:%02X:%02X:%02X:%02X:%02X", mac[0], mac[1], mac[2], mac[3], mac[4], mac[5]);
        printf("Result[%d]: %s, % 4s, %d, %d, %d, %s\r\n", i, macAddress, SecurityTypeName(info.securityType), info.rssi, info.band, info.frequency, info.ssid);
    }
}

static int g_scanDone = 0;
void OnWifiScanStateChanged(int state, int size) {
    printf("%s %d, state = %X, size = %d\r\n", __FUNCTION__, __LINE__, state, size);

    if (state == WIFI_STATE_AVALIABLE && size > 0) {
        g_scanDone = 1;
    }
}

static int g_connected = 0;
static void OnWifiConnectionChanged(int state, WifiLinkedInfo *info) {
    if (!info) return;
    PrintLinkedInfo(info);
    if (state == WIFI_STATE_AVALIABLE) {
        g_connected = 1;
    } else {
        g_connected = 0;
    }
}

extern void oled_show_info(void);
void start_sta_connect(char *ssid, char *passwd) {
    printf("start_sta_connect...\r\n");
    int ret = -1;
    // wifi模块初始化，刚刚我们退出了AP模式，需要重新初始化
    ret = hi_wifi_init(2, 2);
    if (ret != 0) {
        printf("hi_wifi_init----%s %d \r\n", __FILE__, __LINE__);
        // return -1;
    }

    WifiErrorCode errCode;

    WifiEvent eventListener = {.OnWifiConnectionChanged = OnWifiConnectionChanged, .OnWifiScanStateChanged = OnWifiScanStateChanged};

    osDelay(10);

    errCode = RegisterWifiEvent(&eventListener);
    printf("rs_errCode: %d---%s----%d\r\n", errCode, __FILE__, __LINE__);

    errCode = EnableWifi();
    printf("rs_errCode: %d---%s----%d\r\n", errCode, __FILE__, __LINE__);
    osDelay(100);

    g_scanDone = 0;
    errCode = Scan();
    printf("rs_errCode: %d---%s----%d\r\n", errCode, __FILE__, __LINE__);

    // wait for scan done!
    while (!g_scanDone) {
        osDelay(5);
    }

    PrintScanResult();

    WifiDeviceConfig apConfig = {};
    int netId = -1;

    // setup your AP params CMCC-zh  ybzy123@zh123456zpc.#$%
    strcpy(apConfig.ssid, ssid);
    if (strcmp(passwd, "0") == 0) {
        apConfig.securityType = WIFI_SEC_TYPE_OPEN;
    } else {
        strcpy(apConfig.preSharedKey, passwd);
        apConfig.securityType = WIFI_SEC_TYPE_PSK;
    }

    errCode = AddDeviceConfig(&apConfig, &netId);
    printf("rs_errCode: %d---%s----%d\r\n", errCode, __FILE__, __LINE__);

    g_connected = 0;
    errCode = ConnectTo(netId);
    printf("rs_errCode: %d---%s----%d\r\n", errCode, __FILE__, __LINE__);

    while (!g_connected) {
        osDelay(10);
    }
    printf("g_connected: %d\r\n", g_connected);
    osDelay(50);

    // 连上了，获取ip...信息
    struct netif *iface = netifapi_netif_find("wlan0");
    if (iface) {
        err_t ret = netifapi_dhcp_start(iface);

        osDelay(200);  // wait DHCP server give me IP
        //打印 IP、网关、子网掩码信息
        ip4_addr_t ip = {0};
        ip4_addr_t netmask = {0};
        ip4_addr_t gw = {0};
        ret = -1;
        ret = netifapi_netif_get_addr(iface, &ip, &netmask, &gw);
        if (ret == ERR_OK) {
            char *ipaddr = ip4addr_ntoa(&ip);
            char *ip0 = "0.0.0.0";
            while (strcmp(ipaddr, ip0) == 0) {
                osDelay(200);
                netifapi_netif_get_addr(iface, &ip, &netmask, &gw);
                ipaddr = ip4addr_ntoa(&ip);
                printf("ip = %s\r\n", ip4addr_ntoa(&ip));
                printf("netmask = %s\r\n", ip4addr_ntoa(&netmask));
                printf("gw = %s\r\n", ip4addr_ntoa(&gw));
            }
            //在oled屏幕上显示ip地址
            OledFillScreen(0x00);
            static char text[128] = {0};
            snprintf(text, sizeof(text), "car's ip: %s", ip4addr_ntoa(&ip));
            OledShowString(0, 0, text, 1);

            car_run();
        }
    }
}