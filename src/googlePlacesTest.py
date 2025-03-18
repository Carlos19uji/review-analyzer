import requests
import time

API_KEY = "fb478d6d113d758c063ec6b249094f5db10a6ec99f2d10f6583a220f6faf7199"  # Your SerpAPI key
GOOGLE_API_KEY = "AIzaSyBzkUi-CSSBdrXY7vJmU3vgNYYm-iGl3PM"  # Your Google Places API key
PLACE_NAME = "Yugo Melbourn Point"  # Change this to any restaurant name
LOCATION = "Cork, Ireland"  # Optional: Specify city for better accuracy

GOOGLE_PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json"

def get_place_id(place_name, location, api_key):
    params = {
        "input": f"{place_name}, {location}",
        "inputtype": "textquery",
        "fields": "place_id",
        "key": api_key
    }
    
    response = requests.get(GOOGLE_PLACES_SEARCH_URL, params=params)
    data = response.json()

    if response.status_code == 200 and "candidates" in data and len(data["candidates"]) > 0:
        return data["candidates"][0]["place_id"]
    else:
        print("Error fetching Place ID:", data)
        return None

# Function to fetch reviews
def get_reviews(place_id, api_key):
    reviews = []
    url = f"https://serpapi.com/search.json?engine=google_maps_reviews&place_id={place_id}&api_key={api_key}"
    
    while True:
        # Send the request to SerpAPI
        response = requests.get(url)
        data = response.json()
        
        # Check if the request was successful
        if response.status_code != 200:
            print(f"Error fetching reviews: {data}")
            break
        
        # Append reviews to the list
        reviews.extend(data.get('reviews', []))
        
        # Check for the next page of reviews
        next_page_token = data.get('next_page_token')
        if next_page_token:
            print("Fetching next page of reviews...")
            time.sleep(2)  # Delay to avoid hitting rate limits
            url = f"https://serpapi.com/search.json?engine=google_maps_reviews&place_id={place_id}&api_key={api_key}&next_page_token={next_page_token}"
        else:
            break  # Exit loop if no more reviews are available
    
    return reviews

# Fetch the place ID
place_id = get_place_id(PLACE_NAME, LOCATION, GOOGLE_API_KEY)

# Fetch reviews using the place ID
if place_id:
    reviews = get_reviews(place_id, API_KEY)

    # Print the reviews
    print(f"Total reviews found: {len(reviews)}")
    for review in reviews:
        print(f"👤 {review['user']['name']}: {review['snippet']} ⭐ {review['rating']}/5")
else:
    print("Place ID not found. Exiting...")

