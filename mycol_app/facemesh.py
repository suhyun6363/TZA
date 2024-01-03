import os
import sys
import cv2
import mediapipe as mp
import numpy as np
import imageio.v2 as imageio
from collections import Counter
from sklearn.cluster import KMeans
from sklearn.preprocessing import StandardScaler
from scipy.spatial import distance
import matplotlib.pyplot as plt
from colormath.color_objects import sRGBColor, LabColor, HSVColor
from colormath.color_conversions import convert_color
from django.core.files.base import ContentFile

sys.path.append(os.path.join(os.path.dirname(__file__), 'config'))
os.environ.setdefault("DJANGO_SETTINGS_MODULE", "config.settings")

from mycol_app.models import UploadedImage
from mycol_app.models import Analysis
from config.settings import BASE_DIR
from django.conf import settings

mp_drawing = mp.solutions.drawing_utils
mp_drawing_styles = mp.solutions.drawing_styles
mp_face_mesh = mp.solutions.face_mesh

def process_uploaded_image(uploaded_images):

    # 이미지 파일의 경우를 사용하세요.
    uploaded_images = UploadedImage.objects.order_by('-id').first()
    IMAGE_FILES = [uploaded_images]

    # facemesh
    with mp.solutions.face_mesh.FaceMesh(
            static_image_mode=True,
            max_num_faces=1,
            refine_landmarks=True,
            min_detection_confidence=0.5) as face_mesh:
        # for idx, file in enumerate(IMAGE_FILES):
        for idx, uploaded_image in enumerate(IMAGE_FILES):
            # image = cv2.imread(file)
            image_path = uploaded_image.image.path
            file_name = os.path.basename(image_path)
            print("File Name:", file_name)

            image = cv2.imread(image_path)

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

                # 얼굴 윤곽선 바깥 영역을 검정색으로 채우기
                inside_mask = np.zeros_like(annotated_image)
                inside_mask = cv2.fillPoly(inside_mask, [face_outline_points], (255, 255, 255))

                # 얼굴 부분만을 따로 잘라내기
                face_inside = cv2.bitwise_and(image, inside_mask)

                cv2.fillConvexPoly(face_only, left_eye_points, color=(0, 0, 0))
                cv2.fillConvexPoly(face_only, right_eye_points, color=(0, 0, 0))
                cv2.fillConvexPoly(face_only, left_eyebrow_points, color=(0, 0, 0))
                cv2.fillConvexPoly(face_only, right_eyebrow_points, color=(0, 0, 0))
                cv2.fillConvexPoly(face_only, lip_points, color=(0, 0, 0))
                cv2.fillConvexPoly(face_only, left_nostril_points, color=(0, 0, 0))
                cv2.fillConvexPoly(face_only, right_nostril_points, color=(0, 0, 0))

            #cv2.imshow('Face Only', face_only)
            #cv2.waitKey(0)
            cv2.destroyAllWindows()

            # 이미지 저장 경로 설정
            output_directory = os.path.join(settings.MEDIA_ROOT)
            os.makedirs(output_directory, exist_ok=True)

            # 이미지 파일명 설정
            output_filepath = os.path.join(output_directory, f'face_analysis.png')
            output_filepath2 = os.path.join(output_directory, f'face_draping.png')

            # face_only 이미지 저장
            cv2.imwrite(output_filepath, face_only)

            # face_inside 이미지 저장
            cv2.imwrite(output_filepath2, face_inside)

    # face_only 이미지 로드
    face_image = imageio.imread(output_filepath)

########## kmeans ##############
    # [0, 0, 0]인 픽셀 제거
    non_black_pixels = face_image[~np.all(face_image == [0, 0, 0], axis=-1)]

    # 이미지를 2D 배열로 변환
    face_data = non_black_pixels.reshape((-1, 3))

    # 표준화 (Standardization) - 평균이 0, 표준편차가 1이 되도록 스케일 조정
    scaler = StandardScaler()
    face_data_scaled = scaler.fit_transform(face_data)

    # 최적의 k 값으로 k-means 클러스터링 수행
    optimal_k = 8
    optimal_kmeans = KMeans(n_clusters=optimal_k, random_state=42)
    optimal_cluster_labels = optimal_kmeans.fit_predict(face_data_scaled)

    # 클러스터 중심값(RGB 형식) 출력
    cluster_centers_rgb = scaler.inverse_transform(optimal_kmeans.cluster_centers_)

    print(f'=============================전체 클러스터 중심값=============================')
    for i, center in enumerate(cluster_centers_rgb):
        print(f'Cluster {i + 1} Center (RGB): {center}')

    # 각 클러스터에 속하는 픽셀 수 계산
    cluster_sizes = Counter(optimal_cluster_labels)

    print(f'=============================전체 클러스터 픽셀수=============================')

    for i, size in cluster_sizes.items():
        print(f'Cluster {i + 1} Size: {size} pixels')

    total_pixels = sum(cluster_sizes.values())

    # 출력 전체 픽셀 수
    print(f'전체 픽셀 수: {total_pixels}')

    # 클러스터 결과 시각화 (이 예시에서는 3D 플로팅)
    fig = plt.figure()

    # 3D 플로팅
    ax = fig.add_subplot(121, projection='3d')
    ax.scatter(face_data_scaled[:, 0], face_data_scaled[:, 1], face_data_scaled[:, 2], c=optimal_cluster_labels)
    ax.scatter(cluster_centers_rgb[:, 0], cluster_centers_rgb[:, 1], cluster_centers_rgb[:, 2], c='red', marker='X', s=200, label='Cluster Centers')
    ax.set_xlabel('Red')
    ax.set_ylabel('Green')
    ax.set_zlabel('Blue')
    ax.legend()
    ax.set_title('3D Plot')

    # 색상으로 표현
    cluster_centers_image = cluster_centers_rgb.reshape((1, optimal_k, 3))
    cluster_centers_image = cluster_centers_image.astype(np.uint8)
    ax2 = fig.add_subplot(122)
    ax2.imshow(cluster_centers_image, aspect='auto')
    ax2.axis('off')
    ax2.set_title('Cluster Centers as Colors')

    # 가장 픽셀 수가 많은 클러스터 찾기
    most_pixels_cluster = max(cluster_sizes, key=cluster_sizes.get)

    # 첫 번째로 Color distance가 작은 클러스터 찾기
    min_distance_1 = float('inf')
    selected_cluster_1 = None

    for i, center in enumerate(cluster_centers_rgb):
        if i != most_pixels_cluster:  # 가장 픽셀 수가 많은 클러스터는 제외
            current_distance = distance.euclidean(cluster_centers_rgb[most_pixels_cluster], center)
            if current_distance < min_distance_1:
                min_distance_1, selected_cluster_1 = current_distance, i

    # 두 번째로 Color distance가 작은 클러스터 찾기
    min_distance_2 = float('inf')
    selected_cluster_2 = None

    for i, center in enumerate(cluster_centers_rgb):
        if i != most_pixels_cluster and i != selected_cluster_1:  # 가장 픽셀 수가 많은 클러스터와 첫 번째로 선택된 클러스터는 제외
            current_distance = distance.euclidean(cluster_centers_rgb[most_pixels_cluster], center)
            if current_distance < min_distance_2:
                min_distance_2, selected_cluster_2 = current_distance, i

    # 각 클러스터에 속하는 픽셀의 수를 RGB값에 곱한 뒤, 세 개의 클러스터의 픽셀 수로 나누어 평균 RGB 값을 계산
    most_pixels_cluster_size = cluster_sizes[most_pixels_cluster]
    selected_cluster_1_size = cluster_sizes[selected_cluster_1]
    selected_cluster_2_size = cluster_sizes[selected_cluster_2]

    most_pixels_rgb_sum = np.sum(face_data[optimal_cluster_labels == most_pixels_cluster], axis=0)
    selected_cluster_1_rgb_sum = np.sum(face_data[optimal_cluster_labels == selected_cluster_1], axis=0)
    selected_cluster_2_rgb_sum = np.sum(face_data[optimal_cluster_labels == selected_cluster_2], axis=0)

    most_pixels_rgb_mean_weighted = most_pixels_rgb_sum / most_pixels_cluster_size
    selected_cluster_1_rgb_mean_weighted = selected_cluster_1_rgb_sum / selected_cluster_1_size
    selected_cluster_2_rgb_mean_weighted = selected_cluster_2_rgb_sum / selected_cluster_2_size

    total_rgb_sum_weighted = (most_pixels_rgb_sum + selected_cluster_1_rgb_sum + selected_cluster_2_rgb_sum)
    total_rgb_mean_weighted = total_rgb_sum_weighted / (most_pixels_cluster_size + selected_cluster_1_size + selected_cluster_2_size)

    print(f'=============================Color distance계산=============================')

    # 결과 출력
    print(f'가장 픽셀 수가 많은 클러스터: {most_pixels_cluster + 1}')
    print(f'첫 번째로 Color distance가 작은 클러스터: {selected_cluster_1 + 1}')
    print(f'두 번째로 Color distance가 작은 클러스터: {selected_cluster_2 + 1}')
    print(f'첫 번째 Color distance: {min_distance_1}')
    print(f'두 번째 Color distance: {min_distance_2}')

    print(f'=============================가중평균=============================')
    print(f'가장 픽셀 수가 많은 클러스터의 가중 평균 RGB 값: {most_pixels_rgb_mean_weighted}')
    print(f'첫 번째로 Color distance가 작은 클러스터의 가중 평균 RGB 값: {selected_cluster_1_rgb_mean_weighted}')
    print(f'두 번째로 Color distance가 작은 클러스터의 가중 평균 RGB 값: {selected_cluster_2_rgb_mean_weighted}')
    print(f'세개의 클러스터의 가중 평균 RGB 값: {total_rgb_mean_weighted}')


    # 각 클러스터에 해당하는 이미지 생성 및 출력
    fig, axes = plt.subplots(1, optimal_k + 1, figsize=(15, 3))

    # 원본 이미지
    axes[0].imshow(face_image)
    axes[0].axis('off')
    axes[0].set_title('Original Image')

    for i in range(optimal_k):
        cluster_indices = np.where(optimal_cluster_labels == i)
        cluster_pixels = face_data[cluster_indices]
        cluster_image = np.zeros_like(face_data)
        cluster_image[cluster_indices] = cluster_pixels

        # 이미지 재구성
        cluster_image_reshaped = cluster_image.reshape(non_black_pixels.shape)
        result_image = np.zeros_like(face_image)
        result_image[~np.all(face_image == [0, 0, 0], axis=-1)] = cluster_image_reshaped

        axes[i + 1].imshow(result_image)
        axes[i + 1].axis('off')
        axes[i + 1].set_title(f'Cluster {i + 1}')


    # 결과 색상 출력
    fig, ax = plt.subplots(1, 4, figsize=(16, 4))

    ax[0].imshow(most_pixels_rgb_mean_weighted.reshape((1, 1, 3)) / 255)
    ax[0].set_title('Most Pixels ')
    ax[0].axis('off')

    ax[1].imshow(selected_cluster_1_rgb_mean_weighted.reshape((1, 1, 3)) / 255)
    ax[1].set_title('1st Smallest Color Distance ')
    ax[1].axis('off')

    ax[2].imshow(selected_cluster_2_rgb_mean_weighted.reshape((1, 1, 3)) / 255)
    ax[2].set_title('2nd Smallest Color Distance ')
    ax[2].axis('off')

    ax[3].imshow(total_rgb_mean_weighted.reshape((1, 1, 3)) / 255)
    ax[3].set_title('Total Weighted Mean Color')
    ax[3].axis('off')

    #plt.show()

    print(f'=============================변환=============================')
    # 평균 RGB 값 출력
    print(f'평균 RGB 값: {total_rgb_mean_weighted}')

    # RGB to Lab conversion
    average_srgb = sRGBColor(total_rgb_mean_weighted[0], total_rgb_mean_weighted[1], total_rgb_mean_weighted[2], is_upscaled=True)
    average_lab = convert_color(average_srgb, LabColor)

    # 출력 Lab 값
    print(f'Lab: L={average_lab.lab_l}, a={average_lab.lab_a}, b={average_lab.lab_b}')

    # RGB to HSV conversion
    average_hsv = convert_color(average_srgb, HSVColor)

    # 출력 HSV 값
    # s, v값은 비율입니당~
    print(f'HSV: H={average_hsv.hsv_h}, S={average_hsv.hsv_s}, V={average_hsv.hsv_v}')

    # L, b, S값 출력
    print(f'=============================LbS=============================')
    print(f' L={average_lab.lab_l}, b={average_lab.lab_b}, S={average_hsv.hsv_s}')

    #영역이미지 저장코드추가

    # 현재 스크립트의 디렉토리를 기준으로 상대경로 설정
    project_directory = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")

    # 이미지를 저장할 디렉토리
    output_directory = os.path.join(project_directory, "media", "cluster_images")
    #output_directory = os.path.join(BASE_DIR, "media", "cluster_images")
    os.makedirs(output_directory, exist_ok=True)

    # Analysis 모델에 데이터 저장
    analysis_instance = Analysis.objects.create()

    # 클러스터 이미지 및 Total Weighted Mean Color 이미지 저장
    cluster_images = []  # 클러스터 이미지 파일 경로를 저장할 리스트

    for i, cluster_indices in enumerate([most_pixels_cluster, selected_cluster_1, selected_cluster_2]):
        cluster_pixels = face_data[optimal_cluster_labels == cluster_indices]
        cluster_image = np.zeros_like(face_data)
        cluster_image[optimal_cluster_labels == cluster_indices] = cluster_pixels

        # 이미지 재구성
        cluster_image_reshaped = cluster_image.reshape(non_black_pixels.shape)
        result_image = np.zeros_like(face_image)
        result_image[~np.all(face_image == [0, 0, 0], axis=-1)] = cluster_image_reshaped

        # 이미지 저장
        cluster_filename = f'cluster_{i + 1}.png'
        cluster_filepath = os.path.join(output_directory, cluster_filename)
        imageio.imwrite(cluster_filepath, result_image)

        # 클러스터 이미지 파일 경로를 리스트에 추가
        cluster_images.append(cluster_filepath)

        # 이미지를 File 객체로 변환하여 업로드
        cluster_image_content = ContentFile(result_image.tobytes())
        setattr(analysis_instance, f'cluster_image_{i + 1}', cluster_image_content)
        analysis_instance.save()

    # 클러스터 이미지 파일 경로를 모델에 저장
    analysis_instance.cluster_image_1 = 'cluster_images/cluster_1.png'
    analysis_instance.cluster_image_2 = 'cluster_images/cluster_2.png'
    analysis_instance.cluster_image_3 = 'cluster_images/cluster_3.png'
    analysis_instance.save()

    # Total Weighted Mean Color 이미지 저장
    # 위와 마찬가지로 파일의 상대 경로를 저장합니다.
    analysis_instance.total_weighted_mean_color_image = 'cluster_images/total_weighted_mean_color.png'
    analysis_instance.save()

    # Total Weighted Mean Color 이미지 저장
    total_weighted_mean_color_image = np.zeros((50, 50, 3), dtype=np.uint8)
    total_weighted_mean_color_image[:, :] = total_rgb_mean_weighted.astype(np.uint8)
    total_weighted_mean_color_filename = os.path.join(output_directory, 'total_weighted_mean_color.png')

    # 이 부분은 이미지를 저장할 때도 파일의 상대 경로를 사용해야 합니다.
    imageio.imwrite(total_weighted_mean_color_filename, total_weighted_mean_color_image)
    analysis_instance.total_weighted_mean_color_image = 'cluster_images/total_weighted_mean_color.png'
    analysis_instance.save()

    # 메시지 출력
    print(f'이미지 저장경로: {output_directory}')


####### 추가 #######

    # 주어진 타입의 RGB 값들
    spring_type_rgb = np.array([
        (251, 211, 168), (255, 202, 149),
        (253, 197, 161), (252, 204, 130)
    ])

    summer_type_rgb = np.array([
        (253, 231, 174), (255, 219, 192),
        (254, 217, 170), (254, 210, 122)
    ])

    fall_type_rgb = np.array([
        (255, 221, 150), (247, 206, 152),
        (249, 201, 128), (212, 169, 101)
    ])

    winter_type_rgb = np.array([
        (255, 220, 147), (242, 206, 148),
        (247, 207, 121), (216, 173, 102)
    ])

    # 주어진 타입과 total_rgb_mean_weighted 간의 거리 계산 함수
    def calculate_distance(type_rgb, target_rgb):
        return np.mean(np.linalg.norm(type_rgb - target_rgb, axis=1))

    # 새로운 타입별 거리 계산
    spring_light_distance = calculate_distance(np.array([spring_type_rgb[0], spring_type_rgb[1]]),
                                               total_rgb_mean_weighted)
    spring_clear_distance = calculate_distance(np.array([spring_type_rgb[2], spring_type_rgb[3]]),
                                               total_rgb_mean_weighted)

    summer_light_distance = calculate_distance(np.array([summer_type_rgb[0], summer_type_rgb[1]]),
                                               total_rgb_mean_weighted)
    summer_mute_distance = calculate_distance(np.array([summer_type_rgb[2], summer_type_rgb[3]]),
                                              total_rgb_mean_weighted)

    fall_deep_distance = calculate_distance(np.array([fall_type_rgb[0], fall_type_rgb[1]]), total_rgb_mean_weighted)
    fall_mute_distance = calculate_distance(np.array([fall_type_rgb[2], fall_type_rgb[3]]), total_rgb_mean_weighted)

    winter_clear_distance = calculate_distance(np.array([winter_type_rgb[0], winter_type_rgb[1]]),
                                               total_rgb_mean_weighted)
    winter_deep_distance = calculate_distance(np.array([winter_type_rgb[2], winter_type_rgb[3]]),
                                              total_rgb_mean_weighted)

    # 거리를 토대로 정렬
    distances = [
        ("봄 Light", spring_light_distance),
        ("봄 Clear", spring_clear_distance),
        ("여름 Light", summer_light_distance),
        ("여름 Mute", summer_mute_distance),
        ("가을 Deep", fall_deep_distance),
        ("가을 Mute", fall_mute_distance),
        ("겨울 Clear", winter_clear_distance),
        ("겨울 Deep", winter_deep_distance)
    ]

    distances.sort(key=lambda x: x[1])

    # personal_color 및 second_color 계산
    personal_color = distances[0][0]
    second_color = distances[1][0]

    # personal_color 및 second_color 값 모델에 저장
    analysis_instance.personal_color = personal_color
    analysis_instance.second_color = second_color
    analysis_instance.save()


    # 결과 출력
    # print(f'베스트 퍼스널 컬러: {distances[0][0]}')
    # print(f'세컨드 컬러: {distances[1][0]}')

    print(personal_color)
    print(second_color)



