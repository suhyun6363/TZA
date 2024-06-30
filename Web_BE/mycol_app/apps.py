from django.apps import AppConfig

class MycolAppConfig(AppConfig):
    default_auto_field = 'django.db.models.BigAutoField'
    name = 'mycol_app'

    def ready(self):
        import mycol_app.signals

'''
    def ready(self):
        signals_module_path = os.path.join(os.path.dirname(__file__), 'signals', '__init__.py')
        spec = spec_from_file_location('signals', signals_module_path)
        signals_module = module_from_spec(spec)
        spec.loader.exec_module(signals_module)
'''
