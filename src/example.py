import time
from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from webdriver_manager.chrome import ChromeDriverManager
from selenium.webdriver.common.by import By


# Set up Chrome driver with options to avoid GUI pop-ups
chrome_options = Options()
chrome_options.add_argument("--headless")  # Runs Chrome in headless mode (without GUI)

# Initialize the WebDriver
driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=chrome_options)

# URL of the Google Maps restaurant page
url = "https://www.google.com/search?q=Apache%20Pizza%20Bishopstown%20Rese%C3%B1as&rflfq=1&num=20&stick=H4sIAAAAAAAAAONgkxI2MrYwtgAiYyNDIxMzE0tzc7MNjIyvGBUdCxKTM1IVAjKrqhIVnDKLM_ILikvyy_MUglKLUw9vTCxexEpYDQAuUxYzYgAAAA&rldimm=2383883832124649776&tbm=lcl&cs=1&hl=es&sa=X&ved=0CAkQ9fQKKABqFwoTCIihquXxh4wDFQAAAAAdAAAAABAP&biw=1536&bih=730&dpr=1.25#lkt=LocalPoiReviews"  # Replace with actual URL

# Open the URL
driver.get(url)

# Scroll and load more reviews
reviews = []
last_height = driver.execute_script("return document.body.scrollHeight")

# Loop to scroll down and load more reviews
while True:
    # Wait for reviews to load
    time.sleep(2)
    
    # Get the current reviews
    current_reviews = driver.find_elements(By.CLASS_NAME, "OA1nbd")
    print(current_reviews)
    for review in current_reviews:
        reviews.append(review.text)

    # Scroll down to the bottom to load more reviews
    driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")

    # Wait for the new reviews to load
    time.sleep(3)

    # Check if we have reached the bottom of the page
    new_height = driver.execute_script("return document.body.scrollHeight")
    if new_height == last_height:
        # No more new reviews loaded
        break
    last_height = new_height

# Output the reviews collected
print(f"Total reviews collected: {len(reviews)}")
for idx, review in enumerate(reviews, start=1):
    print(f"Review {idx}: {review}")

# Close the driver
driver.quit()

