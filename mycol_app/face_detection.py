# import dlib
# import cv2
# import numpy as np
#
# #==============================================================================
# #   1. 랜드마크 형식 변환 함수
# #       입력: dlib 형식의 랜드마크
# #       출력: numpy 형식의 랜드마크
# #==============================================================================
# def landmarks_to_np(landmarks, dtype="int"):
#     # landmarks의 수를 가져옴
#     num = landmarks.num_parts
#
#     # (x, y) 좌표의 리스트 초기화
#     coords = np.zeros((num, 2), dtype=dtype)
#
#     # 68개의 얼굴 특징점을 루프 돌면서 (x, y) 좌표로 변환
#     for i in range(0, num):
#         coords[i] = (landmarks.part(i).x, landmarks.part(i).y)
#     # (x, y) 좌표의 리스트 반환
#     return coords
#
#
# # ==============================================================================
# #   2. 회귀선 그리고 동공 찾기 함수
# #       입력: 이미지 및 numpy 형식의 랜드마크
# #       출력: 왼쪽 동공 좌표 및 오른쪽 동공 좌표
# # ==============================================================================
# def get_centers(img, landmarks):
#     # 선형 회귀
#     EYE_LEFT_OUTTER = landmarks[2]
#     EYE_LEFT_INNER = landmarks[3]
#     EYE_RIGHT_OUTTER = landmarks[0]
#     EYE_RIGHT_INNER = landmarks[1]
#
#     x = ((landmarks[0:4]).T)[0]
#     y = ((landmarks[0:4]).T)[1]
#     A = np.vstack([x, np.ones(len(x))]).T
#     k, b = np.linalg.lstsq(A, y, rcond=None)[0]
#
#     x_left = (EYE_LEFT_OUTTER[0] + EYE_LEFT_INNER[0]) / 2
#     x_right = (EYE_RIGHT_OUTTER[0] + EYE_RIGHT_INNER[0]) / 2
#     LEFT_EYE_CENTER = np.array([np.int32(x_left), np.int32(x_left * k + b)])
#     RIGHT_EYE_CENTER = np.array([np.int32(x_right), np.int32(x_right * k + b)])
#
#     pts = np.vstack((LEFT_EYE_CENTER, RIGHT_EYE_CENTER))
#     cv2.polylines(img, [pts], False, (255, 0, 0), 1)  # 画回归线
#     cv2.circle(img, (LEFT_EYE_CENTER[0], LEFT_EYE_CENTER[1]), 3, (0, 0, 255), -1)
#     cv2.circle(img, (RIGHT_EYE_CENTER[0], RIGHT_EYE_CENTER[1]), 3, (0, 0, 255), -1)
#
#     return LEFT_EYE_CENTER, RIGHT_EYE_CENTER
#
#
# # ==============================================================================
# #   3. 얼굴 정렬 함수
# #       입력: 이미지 및 왼쪽 동공 좌표 및 오른쪽 동공 좌표
# #       출력: 정렬된 얼굴 이미지
# # ==============================================================================
# def get_aligned_face(img, left, right):
#     desired_w = 256
#     desired_h = 256
#     desired_dist = desired_w * 0.5
#
#     eyescenter = ((left[0] + right[0]) * 0.5, (left[1] + right[1]) * 0.5)  # 眉心
#     dx = right[0] - left[0]
#     dy = right[1] - left[1]
#     dist = np.sqrt(dx * dx + dy * dy)  # 눈 사이 거리
#     scale = desired_dist / dist  # 스케일 비율
#     angle = np.degrees(np.arctan2(dy, dx))  # 회전 각도
#     M = cv2.getRotationMatrix2D(eyescenter, angle, scale)  # 회전 행렬 계산
#
#     # 행렬의 번역 구성 요소 업데이트
#     tX = desired_w * 0.5
#     tY = desired_h * 0.5
#     M[0, 2] += (tX - eyescenter[0])
#     M[1, 2] += (tY - eyescenter[1])
#
#     aligned_face = cv2.warpAffine(img, M, (desired_w, desired_h))
#
#     return aligned_face
#
#
# # ==============================================================================
# #   4. 안경 착용 여부 판별 함수
# #       입력: 정렬된 얼굴 이미지
# #       출력: 판별 값(True/False)
# # ==============================================================================
# def judge_eyeglass(img):
#     img = cv2.GaussianBlur(img, (11, 11), 0)  # 가우시안 블러
#
#     sobel_y = cv2.Sobel(img, cv2.CV_64F, 0, 1, ksize=-1)  # y방향 소벨 엣지 검출
#     sobel_y = cv2.convertScaleAbs(sobel_y)  # uint8로 변환
#     cv2.imshow('sobel_y', sobel_y)
#
#     edgeness = sobel_y  # 엣지 강도 매트릭스
#
#     # Otsu 이진화
#     retVal, thresh = cv2.threshold(edgeness, 0, 255, cv2.THRESH_BINARY + cv2.THRESH_OTSU)
#
#     # 특징 길이 계산
#     d = len(thresh) * 0.5
#     x = np.int32(d * 6 / 7)
#     y = np.int32(d * 3 / 4)
#     w = np.int32(d * 2 / 7)
#     h = np.int32(d * 2 / 4)
#
#     x_2_1 = np.int32(d * 1 / 4)
#     x_2_2 = np.int32(d * 5 / 4)
#     w_2 = np.int32(d * 1 / 2)
#     y_2 = np.int32(d * 8 / 7)
#     h_2 = np.int32(d * 1 / 2)
#
#     roi_1 = thresh[y:y + h, x:x + w]  # ROI 추출
#     roi_2_1 = thresh[y_2:y_2 + h_2, x_2_1:x_2_1 + w_2]
#     roi_2_2 = thresh[y_2:y_2 + h_2, x_2_2:x_2_2 + w_2]
#     roi_2 = np.hstack([roi_2_1, roi_2_2])
#
#     measure_1 = sum(sum(roi_1 / 255)) / (np.shape(roi_1)[0] * np.shape(roi_1)[1])  # 평가값 계산
#     measure_2 = sum(sum(roi_2 / 255)) / (np.shape(roi_2)[0] * np.shape(roi_2)[1])  # 평가값 계산
#     measure = measure_1 * 0.3 + measure_2 * 0.7
#
#     cv2.imshow('roi_1', roi_1)
#     cv2.imshow('roi_2', roi_2)
#     print(measure)
#
#     # 평가값과 임계값의 관계에 따라 판별 값을 결정
#     if measure > 0.15:  # 임계값 조절 가능, 실험적으로 약 0.15 정도에서 테스트됨
#         judge = True
#     else:
#         judge = False
#     print(judge)
#     return judge
#
# def detect_eyeglass(img, landmarks):
#     # 선형 회귀
#     LEFT_EYE_CENTER, RIGHT_EYE_CENTER = get_centers(img, landmarks)
#
#     # 얼굴 정렬
#     aligned_face = get_aligned_face(img, LEFT_EYE_CENTER, RIGHT_EYE_CENTER)
#     cv2.imshow("aligned_face", aligned_face)
#
#     # 안경 착용 여부 판별
#     img_with_glasses = judge_eyeglass(aligned_face)
#     return img_with_glasses
#
# # ==============================================================================
# #   **************************주 함수 진입***********************************
# # ==============================================================================
#
# predictor_path = "./data/shape_predictor_5_face_landmarks.dat"  # 얼굴 특징점 훈련 데이터 경로
# detector = dlib.get_frontal_face_detector()  # 얼굴 검출기 detector
# predictor = dlib.shape_predictor(predictor_path)  # 얼굴 특징점 검출기 predictor
#
#
# cap = cv2.VideoCapture(0)  # 카메라 시작
#
# while (cap.isOpened()):
#     # 비디오 프레임 읽기
#     _, img = cap.read()
#
#     # 그레이스케일로 변환
#     gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
#
#     # 얼굴 검출
#     rects = detector(gray, 1)
#
#     # 각 검출된 얼굴에 대해 조작 수행
#     for i, rect in enumerate(rects):
#         # 좌표 얻기
#         x_face = rect.left()
#         y_face = rect.top()
#         w_face = rect.right() - x_face
#         h_face = rect.bottom() - y_face
#
#         # 경계 상자 그리기 및 텍스트 주석 추가
#         cv2.rectangle(img, (x_face, y_face), (x_face + w_face, y_face + h_face), (0, 255, 0), 2)
#         cv2.putText(img, "Face #{}".format(i + 1), (x_face - 10, y_face - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7,
#                     (0, 255, 0), 2, cv2.LINE_AA)
#
#         # 얼굴 특징점 검출 및 주석 달기
#         landmarks = predictor(gray, rect)
#         landmarks = landmarks_to_np(landmarks)
#         for (x, y) in landmarks:
#             cv2.circle(img, (x, y), 2, (0, 0, 255), -1)
#
#         # 선형 회귀
#         LEFT_EYE_CENTER, RIGHT_EYE_CENTER = get_centers(img, landmarks)
#
#         # 얼굴 정렬
#         aligned_face = get_aligned_face(gray, LEFT_EYE_CENTER, RIGHT_EYE_CENTER)
#         cv2.imshow("aligned_face #{}".format(i + 1), aligned_face)
#
#         # 안경 착용 여부 판별
#         judge = judge_eyeglass(aligned_face)
#         if judge == True:
#             cv2.putText(img, "With Glasses", (x_face + 100, y_face - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 255, 0), 2,
#                         cv2.LINE_AA)
#         else:
#             cv2.putText(img, "No Glasses", (x_face + 100, y_face - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.7, (0, 0, 255), 2,
#                         cv2.LINE_AA)
#
#     # 결과 표시
#     cv2.imshow("Result", img)
#
#     k = cv2.waitKey(5) & 0xFF
#     if k == 27:  # "Esc" 키를 누르면 종료
#         break
#
# cap.release()
# cv2.destroyAllWindows()