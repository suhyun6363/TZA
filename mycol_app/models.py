from django.db import models
#from image_upload.models import UploadedImage   #이미지 업로드 모델 import

# Create your models here.


class UploadedImage(models.Model):
    objects = None
    id = models.AutoField(primary_key=True)
    image = models.ImageField(upload_to='uploads/')
    uploaded_at = models.DateTimeField(auto_now_add=True)




class Analysis(models.Model):
    objects = None
    l_average = models.FloatField(default=0.0)
    b_average = models.FloatField(default=0.0)
    s_average = models.FloatField(default=0.0)
    cluster_image_1 = models.ImageField(upload_to='cluster_images/', null=True, blank=True)
    cluster_image_2 = models.ImageField(upload_to='cluster_images/', null=True, blank=True)
    cluster_image_3 = models.ImageField(upload_to='cluster_images/', null=True, blank=True)
    total_weighted_mean_color_image = models.ImageField(upload_to='cluster_images/', null=True, blank=True)


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