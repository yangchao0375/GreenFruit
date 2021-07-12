# Author:曹涛
# Date:2021/7/9
# UpDate:2021/7/10


import cv2
import numpy as np

frameWidth = 640
frameHeight = 480
cap = cv2.VideoCapture(0)
cap.set(3,frameWidth)
cap.set(4,frameHeight)
cap.set(10,150)
widthImg,heightImg = 640 ,480

def preProcessing(img):
    imgGray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    imgBlur = cv2.GaussianBlur(imgGray,(5,5),1)
    imgCanny = cv2.Canny(imgBlur, 200, 200)
    kernel = np.ones((5,5))
    imgDial = cv2.dilate(imgCanny, kernel, iterations=2)
    imgThers = cv2.erode(imgDial, kernel,iterations=1)

    return  imgThers

def getContours(img):
    biggest = np.array([])
    maxArea = 0
    contours,hierachy = cv2.findContours(img,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)
    for cnt in contours:
        area = cv2.contourArea(cnt)
        print(area)
        if area>1000:
            # cv2.drawContours(imgContour, cnt, -1, (255, 0, 0), 3)
            peri = cv2.arcLength(cnt,True)
            approx = cv2.approxPolyDP(cnt,0.02*peri,True)
            if area > maxArea & len(approx) == 4:
                biggest = approx
                maxArea = area
    cv2.drawContours(imgContour, biggest, -1, (255, 0, 0), 20)
    return  biggest

def getWarp(img,biggest):

    pass

while True:
    success, img = cap.read()
    imgContour = img.copy()
    cv2.resize(img,(widthImg,heightImg))

    imgThers = preProcessing(img)
    biggest = getContours(imgThers)
    print(biggest)
    getWarp(img,biggest)

    cv2.imshow("Result",imgContour)
    if cv2.waitKey(1) & 0xFF == ord('q'):
        break