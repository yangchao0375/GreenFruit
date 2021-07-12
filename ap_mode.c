#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include "../include/oled_ssd1306.h"
#include "../include/udp_mode.h"
#include "cJSON.h"
#include "cmsis_os2.h"
#include "hi_wifi_api.h"
#include "lwip/ip_addr.h"
#include "lwip/netifapi.h"
#include "lwip/sockets.h"
#include "math.h"
#include "ohos_init.h"
#include "string.h"

static struct netif *g_lwip_netif = NULL;

void softap_reset_addr(struct netif *pst_lwip_netif) {
    ip4_addr_t st_gw;
    ip4_addr_t st_ipaddr;
    ip4_addr_t st_netmask;
    if (pst_lwip_netif == NULL) {
        printf("FILE: %d,  LINE: %d, %s\r\n", __FILE__, __LINE__, "pst_lwip_netif == NULL");
        return;
    }
    IP4_ADDR(&st_ipaddr, 0, 0, 0, 0);
    IP4_ADDR(&st_gw, 0, 0, 0, 0);
    IP4_ADDR(&st_netmask, 0, 0, 0, 0);

    netifapi_netif_set_addr(pst_lwip_netif, &st_ipaddr, &st_netmask, &st_gw);
}

void wifi_stop_softap(void) {
    printf("wifi_stop_softap...\r\n");
    int ret;
    netifapi_dhcps_stop(g_lwip_netif);
    softap_reset_addr(g_lwip_netif);

    ret = hi_wifi_softap_stop();
    if (ret != HISI_OK) {
        printf("FILE: %d,  LINE: %d, %s\r\n", __FILE__, __LINE__, "hi_wifi_softap_stop error");
    }

    ret = hi_wifi_deinit();
    if (ret != HISI_OK) {
        printf("FILE: %d,  LINE: %d, %s\r\n", __FILE__, __LINE__, "hi_wifi_deinit error");
    }

    g_lwip_netif = NULL;
}

extern void oled_show_info(void);
int wifi_start_ap(void) {
    printf("wifi_start_ap...\r\n");
    int ret;
    errno_t rc;
    char ifname[WIFI_IFNAME_MAX_SIZE + 1] = {0};
    int len = sizeof(ifname);
    hi_wifi_softap_config hapd_conf = {0};
    ip4_addr_t st_gw;
    ip4_addr_t st_ipaddr;
    ip4_addr_t st_netmask;
    rc = memcpy_s(hapd_conf.ssid, HI_WIFI_MAX_SSID_LEN + 1, "RaceCar-Wifi28883", 17);
    if (rc != EOK) {
        return -1;
    }
    hapd_conf.authmode = HI_WIFI_SECURITY_OPEN;
    hapd_conf.channel_num = 1;
    ret = hi_wifi_softap_start(&hapd_conf, ifname, &len);
    if (ret != HISI_OK) {
        printf("hi_wifi_softap_start\n");
        return -1;
    }
    g_lwip_netif = netifapi_netif_find(ifname);
    if (g_lwip_netif == NULL) {
        printf("FILE: %d,  LINE: %d, %s\r\n", __FILE__, __LINE__, "netifapi_netif_find error");
        return -1;
    }
    IP4_ADDR(&st_gw, 192, 168, 11, 1);
    IP4_ADDR(&st_ipaddr, 192, 168, 11, 1);
    IP4_ADDR(&st_netmask, 255, 255, 255, 0);
    netifapi_netif_set_addr(g_lwip_netif, &st_ipaddr, &st_netmask, &st_gw);
    netifapi_dhcps_start(g_lwip_netif, 0, 0);
    // oled屏幕显示
    OledFillScreen(0x00);
    OledShowString(0, 0, "RaceCar-Wifi28883", 1);

    start_udp_sta_thread();

    return 0;
}
