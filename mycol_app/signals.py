from django.db.models.signals import post_save
from django.dispatch import receiver
from .models import UploadedImage
from .facemesh import process_uploaded_image  # facemesh.py에서 이미지 처리 함수를 import

@receiver(post_save, sender=UploadedImage)
def process_image_on_upload(sender, instance, created, **kwargs):
    if created:  # 이미지가 새로 생성되었을 때만 실행
        process_uploaded_image(instance)
