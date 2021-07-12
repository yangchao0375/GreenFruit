# Author:曹涛
# Date:2021/7/8
# UpDate:2021/7/10

import cv2
import numpy as np
frameWidth = 640
frameHeight = 480
cap = cv2.VideoCapture(0)
cap.set(3,frameWidth)
cap.set(4,frameHeight)
cap.set(10,150)


myColors =[0,70,223,26,255,255]

def findColor(img,myColors):
    imgHSV = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
    lower = np.array(myColors[0:3])
    upper = np.array(myColors[3:6])
    mask = cv2.inRange(imgHSV, lower, upper)
    getContours(mask)
    # cv2.imshow("Img",mask)


def getContours(img):
    contours,hierachy = cv2.findContours(img,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
    number = 0
    for cnt in contours:
        area = cv2.contourArea(cnt)
        print(area)
        if area<1350:
            if area>500:
                # cv2.drawContours(imgResult, cnt, -1, (255, 0, 0), 3)
                peri = cv2.arcLength(cnt,True)
                approx = cv2.approxPolyDP(cnt,0.02*peri,True)
                x , y, w, h = cv2.boundingRect(approx)
                number = number+1
                cv2.rectangle(imgResult, (x, y), (x + w, y + h), (0, 255, 0), 2)
                cv2.putText(imgResult, "Fruit", (x + (w // 2) - 10, y + (h // 2) - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7,
                            (0, 0, 0), 2)
        elif area >1350:
            peri = cv2.arcLength(cnt, True)
            approx = cv2.approxPolyDP(cnt, 0.02 * peri, True)
            x, y, w, h = cv2.boundingRect(approx)
            number = number + 2
            cv2.rectangle(imgResult, (x, y), (x + w, y + h), (0, 255, 0), 2)
            cv2.putText(imgResult, "Fruit", (x + (w // 2) - 10, y + (h // 2) - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7,
                        (0, 0, 0), 2)

    cv2.putText(imgResult,"Fruit Count "+str(number), (280, 440), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 0), 2)
while True:
    success, img = cap.read()
    imgResult = img.copy()
    findColor(img,myColors)
    cv2.imshow("Result",imgResult)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break