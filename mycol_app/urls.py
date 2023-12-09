# mycol_app/urls.py
from django.urls import path
from . import views

urlpatterns = [
    path('analysis/', views.AnalysisList.as_view(), name='analysis_list'),
    path('', views.UploadImageView.as_view(), name='upload_image'),
    path('api/images/<int:image_id>/', views.GetImageView.as_view(), name='get_image'),
]