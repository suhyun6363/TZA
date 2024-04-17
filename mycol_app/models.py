from django.db import models

class UploadedImage(models.Model):
    objects = None
    id = models.AutoField(primary_key=True)
    image = models.ImageField(upload_to='uploads/')
    uploaded_at = models.DateTimeField(auto_now_add=True)

class WearingDetect(models.Model):
    objects = None
    cap_wearing = models.CharField(max_length=50, default="", null=True)
    glasses_wearing = models.CharField(max_length=50, default="", null=True)

class Analysis(models.Model):
    objects = None

    personal_color = models.CharField(max_length=50, default="", null=True)  # 퍼스널 컬러
    second_color = models.CharField(max_length=50, default="", null=True)  # 세컨드 컬러
    v = models.FloatField(default=0.0, null=True)
    b = models.FloatField(default=0.0, null=True)
    s = models.FloatField(default=0.0, null=True)

    cluster_image_1 = models.ImageField(upload_to='cluster_images/', null=True, blank=True)
    cluster_image_2 = models.ImageField(upload_to='cluster_images/', null=True, blank=True)
    cluster_image_3 = models.ImageField(upload_to='cluster_images/', null=True, blank=True)
    total_weighted_mean_color_image = models.ImageField(upload_to='cluster_images/', null=True, blank=True)


    '''
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