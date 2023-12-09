# Generated by Django 4.2.7 on 2023-12-08 06:39

from django.db import migrations, models


class Migration(migrations.Migration):

    dependencies = [
        ('mycol_app', '0002_uploadedimage'),
    ]

    operations = [
        migrations.RenameField(
            model_name='analysis',
            old_name='g_average',
            new_name='l_average',
        ),
        migrations.RenameField(
            model_name='analysis',
            old_name='r_average',
            new_name='s_average',
        ),
        migrations.AddField(
            model_name='analysis',
            name='cluster_image_1',
            field=models.ImageField(blank=True, null=True, upload_to='cluster_images/'),
        ),
        migrations.AddField(
            model_name='analysis',
            name='cluster_image_2',
            field=models.ImageField(blank=True, null=True, upload_to='cluster_images/'),
        ),
        migrations.AddField(
            model_name='analysis',
            name='cluster_image_3',
            field=models.ImageField(blank=True, null=True, upload_to='cluster_images/'),
        ),
        migrations.AddField(
            model_name='analysis',
            name='total_weighted_mean_color_image',
            field=models.ImageField(blank=True, null=True, upload_to='cluster_images/'),
        ),
    ]
