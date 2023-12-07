from django.db import models
#from image_upload.models import UploadedImage   #이미지 업로드 모델 import

# Create your models here.

# 사용자 정의 함수를 만들어 이미지 파일 이름을 특정한 규칙으로 생성
def upload_to(instance, filename):
    # 모델 인스턴스를 저장하여 ID를 할당
    instance.save()

    base_filename, file_extension = os.path.splitext(filename)
    new_filename = f"captured_image_{instance.id}{file_extension}"
    return f"uploads/{new_filename}"

# UploadedImage 모델 정의
class UploadedImage(models.Model):
    id = models.AutoField(primary_key=True)
    image = models.ImageField(upload_to=upload_to)  # upload_to 함수 사용
    uploaded_at = models.DateTimeField(auto_now_add=True)


    """
class UploadedImage(models.Model):
    id = models.AutoField(primary_key=True)
    image = models.ImageField(upload_to='uploads/')
    uploaded_at = models.DateTimeField(auto_now_add=True)
    """



class Analysis(models.Model):
    objects = None
    r_average = models.FloatField(default=0.0)
    g_average = models.FloatField(default=0.0)
    b_average = models.FloatField(default=0.0)


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