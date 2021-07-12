# Author:曹涛
# Date:2021/7/10
# UpDate:2021/7/10

import cv2
import numpy as np

frameWidth = 1280
frameHeight = 960
cap = cv2.VideoCapture(0)
cap.set(3,frameWidth)
cap.set(4,frameHeight)
cap.set(10,150)

while True:
    success, img = cap.read()
    cv2.imshow("Result",img)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break