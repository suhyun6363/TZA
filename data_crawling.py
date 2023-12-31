# https://github.com/dev-dain/Lalavla-Crawling/blob/master/crawling.py 참고

from selenium import webdriver                 # pip install selenium 설치 필요
                                               # 2023년 기준 selenium ver.4.x 설치됨
from selenium.webdriver.common.by import By    # 4.x 버전 이후 find_element호출 방식이 변경되어 추가
import time                                    # sleep() 함수 사용을 위해 추가
from selenium.webdriver.chrome.options import Options
from collections import OrderedDict
import os
import json
import re

url = 'https://www.oliveyoung.co.kr/store/display/getMCategoryList.do?dispCatNo='
data = OrderedDict()

beauty_list = {
  'makeup': {
    '베이스메이크업': {
        '블러셔': '1000001000200010006'
    },
    '립메이크업': {
        '립틴트': '1000001000200060003',
        '립스틱': '1000001000200060004',
        '틴티드_립밤': '1000001000200060001',
        '립글로스': '1000001000200060002'
    },
    '아이메이크업': {
        '아이셰도우': '1000001000200070003'
    }
  }
}

# 브라우저 꺼짐 방지 옵션
chrome_options = Options()
chrome_options.add_experimental_option("detach", True)
# 크롬 드라이버 생성
driver = webdriver.Chrome('./WebDriver/chromedriver.exe')  # 본인이 사용하는 chrome 브라우저 버전에 맞는 chromedriver 다운로드 후 동일 폴더에 저장

def clean_product_name(product_name):
    # 대소문자를 무시하고 숫자 + colors 또는 color를 찾아 삭제
    pattern_to_remove = r'\d+\s*colors?'

    # 대소문자 무시 플래그 추가
    product_name = re.sub(pattern_to_remove, '', product_name, flags=re.IGNORECASE)

    patterns_to_remove = [
        r'\([^)]*\)',  # (내용)
        r'\[[^]]*\]',  # [내용]
        r'\d+종',
        r'택\s*\d+',  # 택 + 숫자 (예: 택 1, 택1)
        r'[+]',  # + (예: 1+1)
        r'\d+g',  # 숫자 + g (예: 4g)
        r'\d+\.\d+g',  # 숫자 + 소수점 + 숫자 + g (예: 4.8g)
        r'\d+ml',  # 숫자 + m 또는 M + g 또는 G (예: 4ml)
        r'\d+\.\d+ml',  # 숫자 + 소수점 + 숫자 + g (예: 4.8ml)
        r'AD',
        r'ad',
        r'단품',
        r'기획',
        r'한정기획',
        r'기획세트',
        r'NEW',
        r'/',           # / (예: /)
    ]

    # 각 패턴을 순회하며 삭제
    for pattern in patterns_to_remove:
        product_name = re.sub(pattern, '', product_name)

    # 중복 공백 제거
    product_name = ' '.join(product_name.split())

    return product_name

def get_product_info(small_list, category):
    '''
    category_list : list(String). 해당 상품의 대/중/소/카테고리     # ex) 메이크업 > 립메이크업(small) > 립틴트(category)
    name : String. 상품 이름
    number : String. 상품 고유번호
    brand : String. 브랜드 이름
    img : String (src). 대표 이미지 -> 복수 개일 수 있으므로 변경 필요
    product_img_list : list(String). 대표 이미지들의 리스트
    is_discount : boolean. 할인 여부
    origin_price : String (**원). 정상가
    discount_price : String (**원). 할인 가격

    옵션이 없는 단일 상품의 경우, 옵션 개수를 0개로 할 것인가 1개로 할 것인가
    그리고 옵션 이름 목록과 가격에 그냥 name과 price를 넣어야 하나?
    품절의 경우 옵션 가격은 얼마?

    option_count : String. 옵션 개수
    option_name_list : list(String). 옵션별 이름
    option_price_list : list(String). 옵션별 가격
    option_img_list : list(String). 옵션별 이미지 src -> colorchip_list
    '''

    time.sleep(0.5)

    number = driver.find_element_by_class_name('prd_btn_area > .btnZzim') \
        .get_attribute('data-ref-goodsno')
    img = (driver.find_element_by_id('mainImg')).get_attribute('src')
    brand = (driver.find_element_by_class_name('prd_brand')).text
    name = (driver.find_element_by_class_name('prd_name')).text
    # 정규표현식을 사용하여 대괄호와 대괄호 안의 내용 삭제
    name = clean_product_name(name)

    cat = driver.find_elements_by_class_name('loc_history > li > .cate_y')
    category_list = [c.text for c in cat]

    discount_price = (driver.find_element_by_class_name('price-2')).text.split('\n')[0]

    try:
        # 할인 상품인 경우 .price-1 요소가 있음
        origin_price = (driver.find_element_by_class_name('price-1')).text.split('\n')[0]
        is_discount = True
    except:
        # 할인이 아닌 경우 discount_price가 곧 origin_price
        # 즉, 어느 경우든 discount_price가 해당 상품의 최종가
        origin_price = discount_price
        is_discount = False

    origin_price = origin_price.replace(',', '')
    discount_price = discount_price.replace(',', '')

    option_name_list = []
    option_price_list = []
    option_img_list = []

    try:
        # 옵션이 없는 경우 .prd_option_box 요소가 없음 (except로 넘어감)
        # .prd_option_box를 클릭해야 .option_value가 드러남
        driver.find_element_by_class_name('prd_option_box').click()

        # 품절 상품인 경우 .type1 soldout임
        options = driver.find_elements_by_class_name('type1 > a > div > .option_value')

        if not options:  # 옵션에 상품 이미지가 없는 경우 .type1 없이 <li class> 태그임
            # options 자체는 WebElement가 요소인 리스트임. option들을 가진 리스트
            options = driver.find_elements_by_tag_name('li > a > div > .option_value')

        # 옵션명, 가격이 차례로 요소로 들어간 리스트가 option_values가 됨
        # ex) ['04 데일리톡(리뉴얼)', '7,840원']
        # 이 때, 품절 상품의 경우 상품 이름만 요소로 들어감. 옵션 자체에 가격이 없음
        option_values = [option.text.split('\n') for option in options]

        # 상품의 옵션 이름만 리스트로 뽑아옴
        option_name_list = [option[0] for option in option_values]
        option_count = len(options)

        for k in range(len(option_name_list)):
            option_name = option_name_list[k]
            # 품절일 경우, 기본가인 discount_price를 품절 상품 가격으로 둠
            if option_name.find('(품절)') != -1:
                option_price_list.append(discount_price)
            else:
                option_price_list.append(option_values[k][1].rstrip('원'))
        option_name_list = [clean_product_name(option[0]) for option in option_values]

        # 옵션은 있으나 옵션에 이미지가 없는 경우 except로 넘어감
        # 옵션에 이미지가 있는 경우에만 option_img_list에 각 이미지를 넣음
        option_imgs = driver.find_elements_by_class_name('type1 > a > span > img')
        option_img_list = [img.get_attribute('src') for img in option_imgs]

    except:
        # 품절일 때 option_count = 0이 돼서 가격이 안 나옴
        option_count = 0

    data["category_list"] = category_list
    data["name"] = name
    data["number"] = number
    data["brand"] = brand
    data["img"] = img
    data["is_discount"] = is_discount
    data["origin_price"] = origin_price
    data["discount_price"] = discount_price
    data["option_count"] = option_count
    data["option_name_list"] = option_name_list
    data["option_price_list"] = option_price_list
    data["colorchip_list"] = option_img_list

    try:
        if len(data["colorchip_list"]) > 0:
            print(data)
            with open(
                    './data/{0}/{1}/{2}.json'.format(small_list, category, number),
                    'w', encoding='utf-8') as f:
                json.dump(data, f, ensure_ascii=False, indent="\t")
    except:  # 디렉터리가 없을 때만 디렉터리를 만듦
        os.makedirs('./data/{0}/{1}'.format(small_list, category))

    driver.back()  # 뒤로가기
    time.sleep(0.5)

for small_list in beauty_list['makeup']:  # small_list는 String
    for category in beauty_list['makeup'][small_list]:  # category는 String
        driver.get(url + beauty_list['makeup'][small_list][category])
        driver.implicitly_wait(1)
        count = 0  # count로 몇 번째 item의 정보를 가져올지 정함

        # 상품을 감싼 태그를 빼냄. 24/36/48개
        items = driver.find_elements_by_xpath('//li[@criteo-goods]')

        for index in range(len(items)):
            # 개별 아이템을 고름
            item = driver.find_element_by_xpath(
                '//*[@id="Contents"]/ul[%s]/li[%s]/div/a'
                % ((count // 4) + 2, (count % 4) + 1)
            )
            item.click()
            get_product_info(small_list, category)
            count += 1

        page_index = 0
        # #Container > div.pageing > a:nth-child(2)
        pages = driver.find_elements_by_css_selector('#Container > div.pageing > a')
        pages_count = len(pages)
        if pages_count > 0:
            while True:
                pages = driver.find_elements_by_css_selector('#Container > div.pageing > a')
                pages[page_index].click()
                time.sleep(0.5)
                count = 0  # count로 몇 번째 item의 정보를 가져올지 정함

                # 상품을 감싼 태그를 빼냄. 24/36/48개
                items = driver.find_elements_by_xpath('//li[@criteo-goods]')

                for index in range(len(items)):
                    # 개별 아이템을 고름
                    item = driver.find_element_by_xpath(
                        '//*[@id="Contents"]/ul[%s]/li[%s]/div/a'
                        % ((count // 4) + 2, (count % 4) + 1)
                    )
                    item.click()
                    get_product_info(small_list, category)
                    count += 1

                page_index += 1

                # 마지막 페이지인 경우 종료
                if page_index == pages_count:
                    break

driver.quit()
