# mycol_app/urls.py
from django.urls import path
from . import views

urlpatterns = [
    path('analysis/', views.AnalysisList.as_view(), name='analysis_list'),
    path('', views.UploadImageView.as_view(), name='upload_image'),
    path('api/images/<int:image_id>/', views.GetImageView.as_view(), name='get_image'),
    path('analysis/latest_analysis/', views.GetLatestAnalysisView.as_view(), name='get_latest_analysis'),
    path('wearing/', views.WearingDetectView.as_view(), name='wearingDetect_list'),
    path('wearing/latest', views.GetLatestDetectView.as_view(), name='get_latest_wearingDetect'),
]