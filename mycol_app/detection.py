import os
import subprocess
from django.conf import settings
import cv2
import mediapipe as mp
import numpy as np

from mycol_app.models import WearingDetect

def detect_uploaded_image(uploaded_image_instance):
    # 업로드된 이미지의 경로를 가져옴
    image_path = uploaded_image_instance.image.path
    cap_detected = False
    glasses_detected = False

    # 훈련한 모델 실행
    command = f"python config/yolov5/detect.py --source {image_path} --weights config/yolov5/static/best_50.pt --img-size 640 --save-txt --save-conf --exist-ok --project {settings.BASE_DIR} --save-crop"
    # command = f"python config/detect.py --source {image_path} --weights config/static/best_50.pt --img-size 640 --save-txt --save-conf --exist-ok --project {settings.BASE_DIR} --save-crop"

    result = subprocess.run(command, shell=True, stdout=subprocess.PIPE, text=True)
    reference_vals = result.stdout.strip().split('\n')

    # object detection한 이미지 파일 facemesh 처리하기
    image_path = os.path.join(settings.BASE_DIR, 'object_detection.jpg')

    # IMAGE_FILES 리스트에 이미지 경로를 추가합니다.
    IMAGE_FILES = [image_path]
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

            for face_landmarks in results.multi_face_landmarks:
                # 얼굴 전체에 대한 랜드마크 그리기
                for landmark in face_landmarks.landmark:
                    x, y = int(landmark.x * image.shape[1]), int(landmark.y * image.shape[0])
                    # cv2.circle(annotated_image, (x, y), 1, (0, 255, 0), 1)

                # 눈썹
                left_eyebrow = [336, 296, 334, 293, 300, 276, 283, 282, 295, 285]
                right_eyebrow = [70, 63, 105, 66, 107, 55, 65, 52, 53, 46]

                # 이마
                forehead = [54, 68, 104, 69, 108, 151, 337, 299, 333, 298, 284, 332, 297, 338, 10, 109, 67, 103]

                face_outline = [10, 338, 297, 332, 284, 251, 389, 356, 454, 323, 361, 288, 397, 365, 379, 378, 400, 377,
                                152, 148, 176, 149, 150, 136, 172, 58, 132, 93, 234, 127, 162, 21, 54, 103, 67, 109]

                left_eyebrow_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]),
                                                 int(face_landmarks.landmark[i].y * image.shape[0])) for i in
                                                left_eyebrow])
                right_eyebrow_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]),
                                                  int(face_landmarks.landmark[i].y * image.shape[0])) for i in
                                                 right_eyebrow])

                forehead_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]),
                                             int(face_landmarks.landmark[i].y * image.shape[0])) for i in forehead])

                # 눈썹 부분을 흰색으로 칠함
                eyebrow_mask = np.zeros_like(image)
                eyebrow_mask = cv2.fillPoly(eyebrow_mask, [left_eyebrow_points, right_eyebrow_points], (255, 255, 255))

                # 이마 부분을 흰색으로 칠함
                forehead_mask = np.zeros_like(image)
                forehead_mask = cv2.fillPoly(forehead_mask, [forehead_points], (255, 255, 255))

                # 눈썹과 이마 부분을 병합
                combined_mask = cv2.addWeighted(eyebrow_mask, 1, forehead_mask, 1, 0)

                # 결과 이미지에 병합된 마스크 추가
                result_image = cv2.addWeighted(image, 1, combined_mask, 0.5, 0)

                face_outline_points = np.array([(int(face_landmarks.landmark[i].x * image.shape[1]),
                                                 int(face_landmarks.landmark[i].y * image.shape[0])) for i in
                                                face_outline])

            print("reference vals:", reference_vals)

            wearing_instance = WearingDetect.objects.create()

            # bounding box 관련 값들 처리하는 코드
            # 모자 감지 처리
            for reference_val in reference_vals:
                center_part_start = reference_val.find("Center: ")
                if center_part_start != -1:
                    center_part_end = reference_val.find(")", center_part_start)
                    if center_part_end != -1:
                        center_part = reference_val[center_part_start + len("Center: "):center_part_end + 1]

                        # 중앙값 좌표값 추출
                        center_coordinates = center_part.strip('()').split(', ')
                        center_x = float(center_coordinates[0])
                        center_y = float(center_coordinates[1])

                        point = (center_x, center_y)
                        #print(point)

                        # label 추출
                        label_str = next((val for val in reference_vals if 'label: ' in val), None)
                        label_value = label_str.split(': ')[1].split(',')[0].strip()

                        # 사각형 좌표 추출
                        # rect_str = next((val for val in reference_vals if "xyxy :" in val), None)
                        #
                        # coordinates_str = rect_str.split("xyxy : ")[1].strip()
                        # rect_coordinates = coordinates_str.split(', ')
                        #
                        # # 좌표값 추출
                        # upperLeftX = float(rect_coordinates[0])
                        # upperLeftY = float(rect_coordinates[1])
                        # lowerRightX = float(rect_coordinates[2])
                        # lowerRightY = float(rect_coordinates[3])
                        #
                        # #######################################
                        # # 모자의 bb안에 이마영역이 포함됐는지 확인
                        #
                        # # 받아온 xyxy좌표로 bounding box 생성
                        # bounding_box = {
                        #     'upper_left': {'x': upperLeftX, 'y': upperLeftY},
                        #     'lower_right': {'x': lowerRightX, 'y': lowerRightY}
                        # }
                        #
                        # if label_value == 'Cap':
                        #     # forehead_points가 bounding_box 안에 있는지 확인
                        #     is_inside_bb = all(
                        #         bounding_box['upper_left']['x'] <= x <= bounding_box['lower_right']['x'] and
                        #         bounding_box['upper_left']['y'] <= y <= bounding_box['lower_right']['y']
                        #         for x, y in forehead_points)
                        #     print("is_inside_bb (Cap):", is_inside_bb)
                        #
                        #     if is_inside_bb:
                        #         print("************모자를 빼주세요!!!************")
                        #         wearing_instance.cap_wearing = '모자를 빼주세요!'
                        #         wearing_instance.save()
                        #         cap_detected = True

            # 안경 감지 처리
            for reference_val in reference_vals:
                center_part_start = reference_val.find("Center: ")
                if center_part_start != -1:
                    center_part_end = reference_val.find(")", center_part_start)
                    if center_part_end != -1:
                        center_part = reference_val[center_part_start + len("Center: "):center_part_end + 1]

                        # 중앙값 좌표값 추출
                        center_coordinates = center_part.strip('()').split(', ')
                        center_x = float(center_coordinates[0])
                        center_y = float(center_coordinates[1])

                        point = (center_x, center_y)
                        #print(point)

                        # label 추출
                        label_str = next((val for val in reference_vals if 'label: ' in val), None)
                        label_value = label_str.split(': ')[1].split(',')[0].strip()

                        # (center_x, center_y) 좌표가 face_outline_points 안에 있는지 확인
                        is_inside_face = cv2.pointPolygonTest(np.array(face_outline_points), point, False) >= 0

                        # (center_x, center_y) 좌표가 left_eyebrow_points 또는 right_eyebrow_points 아래에 있는지 확인(안경)
                        is_under_left_eyebrow = cv2.pointPolygonTest(np.array(left_eyebrow_points), point, False) < 0
                        is_under_right_eyebrow = cv2.pointPolygonTest(np.array(right_eyebrow_points), point,
                                                                      False) < 0

                        # 안경bb의 중앙값이 눈썹 아래에 있는 경우 "안경을 빼주세요!" 출력
                        if label_value == 'Glasses' and is_inside_face and (
                                is_under_left_eyebrow or is_under_right_eyebrow):
                            print("************안경을 빼주세요!!!************")
                            wearing_instance.glasses_wearing = '안경을 빼주세요!'
                            wearing_instance.save()
                            glasses_detected = True

            # 결과를 저장할 디렉토리 지정
            output_directory = os.path.join(settings.MEDIA_ROOT, 'processed_images')
            os.makedirs(output_directory, exist_ok=True)

            # 저장할 파일 이름 지정 (파일 이름을 원하는 대로 지정)
            output_filepath = os.path.join(output_directory, f'face_processed.png')

            # 결과 이미지를 저장
            cv2.imwrite(output_filepath, result_image)

            return cap_detected, glasses_detected

