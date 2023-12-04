# Django 설정 초기화 코드 추가
import os
import django

# 프로젝트 디렉토리의 경로 추가
os.environ.setdefault('DJANGO_SETTINGS_MODULE', 'config.settings')
django.setup()

# 엘보우.ver
import numpy as np
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans
from sklearn.datasets import load_sample_image
from sklearn.preprocessing import StandardScaler
import imageio.v2 as imageio

from mycol_app.models import analysis

# 이미지 로드
image_path = "static/face_only0.png"
face_image = imageio.imread(image_path)

# [0, 0, 0]인 픽셀 제거
non_black_pixels = face_image[~np.all(face_image == [0, 0, 0], axis=-1)]

# 이미지를 2D 배열로 변환
face_data = non_black_pixels.reshape((-1, 3))

# 표준화 (Standardization) - 평균이 0, 표준편차가 1이 되도록 스케일 조정
scaler = StandardScaler()
face_data_scaled = scaler.fit_transform(face_data)

# 최적의 클러스터 개수를 결정하기 위한 k 값 범위 설정
k_values = range(2, 11)

# 각 k 값에 대한 클러스터 내 제곱합(SSW) 저장
inertia_values = []

# k 값에 대한 k-means 클러스터링 수행 및 SSW 계산
for k in k_values:
    kmeans = KMeans(n_clusters=k, random_state=42)
    kmeans.fit(face_data_scaled)
    inertia_values.append(kmeans.inertia_)

# SSW를 플로팅하여 엘보우 지점 확인
#plt.plot(k_values, inertia_values, marker='o')
#plt.xlabel('Number of Clusters (k)')
#plt.ylabel('Within-Cluster Sum of Squares (SSW)')
#plt.title('Elbow Method for Optimal k')
#plt.show()

# 엘보우 지점 확인하여 최적의 k 값 결정
optimal_k = 5  # 값 직접 입력하긔
print(f'Optimal number of clusters (k): {optimal_k}')

# 최적의 k 값으로 k-means 클러스터링 수행
optimal_kmeans = KMeans(n_clusters=optimal_k, random_state=42)
optimal_cluster_labels = optimal_kmeans.fit_predict(face_data_scaled)

# 클러스터 결과 시각화 (이 예시에서는 3D 플로팅)
fig = plt.figure()
ax = fig.add_subplot(111, projection='3d')
ax.scatter(face_data_scaled[:, 0], face_data_scaled[:, 1], face_data_scaled[:, 2], c=optimal_cluster_labels)
ax.set_xlabel('Red')
ax.set_ylabel('Green')
ax.set_zlabel('Blue')
#plt.show()

from collections import Counter

# 최적의 k 값으로 k-means 클러스터링 수행
optimal_k = 5
optimal_kmeans = KMeans(n_clusters=optimal_k, random_state=42)
optimal_cluster_labels = optimal_kmeans.fit_predict(face_data_scaled)

# 클러스터 중심값(RGB 형식) 출력
cluster_centers_rgb = scaler.inverse_transform(optimal_kmeans.cluster_centers_)

for i, center in enumerate(cluster_centers_rgb):
    print(f'Cluster {i + 1} Center (RGB): {center}')

# 각 클러스터에 속하는 픽셀 수 계산
cluster_sizes = Counter(optimal_cluster_labels)

for i, size in cluster_sizes.items():
    print(f'Cluster {i + 1} Size: {size} pixels')

total_pixels = sum(cluster_sizes.values())

# 출력 전체 픽셀 수
print(f'전체 픽셀 수: {total_pixels}')

# 각 클러스터에 속하는 픽셀 수와 중심값(RGB)의 가중 평균 계산
weighted_sum = np.zeros(3)  # 초기화: R, G, B의 가중 합
for i in range(optimal_k):
    cluster_size = cluster_sizes[i]
    cluster_center_rgb = cluster_centers_rgb[i]
    weighted_sum += cluster_center_rgb * cluster_size

# 평균 RGB 계산
average_rgb = weighted_sum / total_pixels

# 출력
print(f'평균 RGB 값: {average_rgb}')

# 평균 RGB 값을 사용하여 이미지 생성
average_rgb_image = np.full((100, 100, 3), average_rgb, dtype=np.uint8)


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

#plt.show()

# 이미지 표시
plt.imshow(average_rgb_image)
plt.axis('off')
plt.title('Average RGB Color')
#plt.show()

from collections import Counter
from colormath.color_objects import sRGBColor, LabColor, HSVColor
from colormath.color_conversions import convert_color


# 평균 RGB 값 출력
print(f'평균 RGB 값: {average_rgb}')

# RGB to Lab conversion
average_srgb = sRGBColor(average_rgb[0], average_rgb[1], average_rgb[2], is_upscaled=True)
average_lab = convert_color(average_srgb, LabColor)

# 출력 Lab 값
print(f'평균 Lab 값: L={average_lab.lab_l}, a={average_lab.lab_a}, b={average_lab.lab_b}')

# RGB to HSV conversion
average_hsv = convert_color(average_srgb, HSVColor)

# 출력 HSV 값
# s, v값은 비율입니당~
print(f'평균 HSV 값: H={average_hsv.hsv_h}, S={average_hsv.hsv_s}, V={average_hsv.hsv_v}')

# L, b, S값 출력
print('=================')
#print(f' L={average_lab.lab_l}, b={average_lab.lab_b}, S={average_hsv.hsv_s}')
print(f'L={average_lab.lab_l}')
print(f'b={average_lab.lab_b}')
print(f'S={average_hsv.hsv_s}')

# 엘보우 메서드를 통해 계산된 L, b, S 값
l_value = average_lab.lab_l
b_value = average_lab.lab_b
s_value = average_hsv.hsv_s

# diagnosis 모델에 데이터 저장
analysis_instance = analysis.objects.create(
    l_value=l_value,
    b_value=b_value,
    s_value=s_value
)