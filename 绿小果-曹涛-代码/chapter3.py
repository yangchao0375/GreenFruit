# Author:曹涛
# Date:2021/7/6
# UpDate:2021/7/6

import cv2
import numpy as np

img = cv2.imread("source/test1.jpeg")
print(img.shape)

imgResize = cv2.resize(img,(480,400))
print(imgResize.shape)

imgCropped = img[0:200,200:500]


cv2.imshow("Image",img)
cv2.imshow("Image Resize",imgResize)
cv2.imshow("Imagr Cropped",imgCropped)

cv2.waitKey(0)