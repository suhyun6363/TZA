import cv2
import mediapipe as mp
import numpy as np

mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles
mp_face_mesh = mp.solutions.face_mesh

# 이미지 파일의 경우를 사용하세요.
IMAGE_FILES = ["static/facemesh.jpg"]

with mp.solutions.face_mesh.FaceMesh(
        static_image_mode=True,
        max_num_faces=1,
        refine_landmarks=True,
        min_detection_confidence=0.5) as face_mesh:

    for idx, file in enumerate(IMAGE_FILES):
        image = cv2.imread(file)

        # 작업 전에 BGR 이미지를 RGB로 변환합니다.
        results = face_mesh.process(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))

        # 이미지에 출력하고 그 위에 얼굴 그물망 경계점을 그립니다.
        if not results.multi_face_landmarks:
            continue

        annotated_image = image.copy()

        for face_landmarks in results.multi_face_landmarks:
            # 얼굴 전체에 대한 랜드마크 그리기
            for landmark in face_landmarks.landmark:
                x, y = int(landmark.x * image.shape[1]), int(landmark.y * image.shape[0])
                #cv2.circle(annotated_image, (x, y), 1, (0, 255, 0), 1)

            # 얼굴의 왼쪽 눈과 오른쪽 눈을 검정색으로 채우기

            # eye indices
            left_eye = [243, 190, 56, 28, 27, 29, 30, 247, 226, 31, 228, 229, 230, 231, 232, 233, 243]
            right_eye = [463, 453, 452, 451, 450, 449, 448, 261, 446, 467, 260, 259, 257, 258, 286, 414, 463]
            # eyebrow indices
            left_eyebrow = [336, 296, 334, 293, 300, 276, 283, 282, 295, 285]
            right_eyebrow = [70, 63, 105, 66, 107, 55, 65, 52, 53, 46]
            # lips indices
            # lips = [61, 185, 40, 39, 37, 0, 267, 269, 270, 409, 291, 375, 321, 405, 314, 17, 84, 181, 91, 146, 61]
            lips = [164, 393, 391, 322, 410, 287, 273, 335, 406, 313, 18,83, 182, 106, 43, 57, 186, 92, 165, 167]
            # nostril indices
            left_nostril = [218, 239, 241, 242, 99, 240, 235, 219]
            right_nostril = [438, 439, 455, 460, 328, 462, 458, 459]
            # face indices
            face_outline = [10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400, 377, 152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109]

            left_eye_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]), int(face_landmarks.landmark[i].y * image.shape[0])) for i in left_eye])
            right_eye_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]), int(face_landmarks.landmark[i].y * image.shape[0])) for i in right_eye])
            left_eyebrow_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]), int(face_landmarks.landmark[i].y * image.shape[0])) for i in left_eyebrow])
            right_eyebrow_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]), int(face_landmarks.landmark[i].y * image.shape[0])) for i in right_eyebrow])
            lip_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]), int(face_landmarks.landmark[i].y * image.shape[0])) for i in lips])
            left_nostril_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]), int(face_landmarks.landmark[i].y * image.shape[0])) for i in left_nostril])
            right_nostril_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]), int(face_landmarks.landmark[i].y * image.shape[0])) for i in right_nostril])
            face_outline_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]), int(face_landmarks.landmark[i].y * image.shape[0])) for i in face_outline])


             # 얼굴 윤곽선 바깥 영역을 검정색으로 채우기
            mask = np.zeros_like(annotated_image)
            outside_mask = cv2.fillPoly(mask, [face_outline_points], (255, 255, 255))

            # 얼굴 부분만을 따로 잘라내기
            face_only = cv2.bitwise_and(image, outside_mask)

            cv2.fillConvexPoly(face_only, left_eye_points, color=(0, 0, 0))
            cv2.fillConvexPoly(face_only, right_eye_points, color=(0, 0, 0))
            cv2.fillConvexPoly(face_only, left_eyebrow_points, color=(0, 0, 0))
            cv2.fillConvexPoly(face_only, right_eyebrow_points, color=(0, 0, 0))
            cv2.fillConvexPoly(face_only, lip_points, color=(0, 0, 0))
            cv2.fillConvexPoly(face_only, left_nostril_points, color=(0, 0, 0))
            cv2.fillConvexPoly(face_only, right_nostril_points, color=(0, 0, 0))

        cv2.imshow('Face Only', face_only)
        cv2.waitKey(0)
        cv2.destroyAllWindows()

        cv2.imwrite('static/face_only' + str(idx) + '.png', face_only)
