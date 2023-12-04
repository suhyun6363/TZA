from django.db import models
#from image_upload.models import UploadedImage   #이미지 업로드 모델 import

# Create your models here.
class analysis(models.Model):
    '''
    #image = models.ForeignKey(UploadedImage, on_delete=models.CASCADE)  #이미지 업로드 모델과의 연결
    personal_color = models.CharField(max_length=50)  #퍼스널 컬러
    second_color = models.CharField(max_length=50)  #세컨드 컬러

    personal_color_palette = models.ImageField(upload_to='personal_color_palettes/', blank=True, null=True)
    second_color_palette = models.ImageField(upload_to='second_color_palettes/', blank=True, null=True)

    #메이크업 추천 정보
    makeup_recommendation = models.TextField()

    #악세서리 추천 정보
    acc_recommendation = models.TextField()

    #헤어 컬러 추천 정보
    hair_color_recommendation = models.CharField(max_length=50)

    #objects = models.Manager()

    def __str__(self):
        return f"Diagnosis: {self.personal_color} - {self.second_color}"
    '''

    objects = None
    average_hsv = models.CharField(max_length=255, null=False, blank=False, default='')
    average_lab = models.CharField(max_length=255, null=False, blank=False, default='')
    average_rgb = models.CharField(max_length=255, null=False, blank=False, default='')

    l_value = models.FloatField(default=0.0)
    b_value = models.FloatField(default=0.0)
    s_value = models.FloatField(default=0.0)