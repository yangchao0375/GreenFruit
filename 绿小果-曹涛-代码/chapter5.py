# Author:曹涛
# Date:2021/7/7
# UpDate:2021/7/7

import cv2
import numpy as np

img = cv2.imread("source/card.jpeg")
width,height = 250,350
pt1 = np.float32([[70,80],[306,80],[70,412],[306,412]])
pt2 = np.float32([[0,0],[width,0],[0,height],[width,height]])
matrix = cv2.getPerspectiveTransform(pt1,pt2)
imgOutPut = cv2.warpPerspective(img,matrix,(width,height))

cv2.imshow("Image",img)
cv2.imshow("OutPutImage",imgOutPut)

cv2.waitKey(0)