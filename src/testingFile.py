import requests
from dotenv import load_dotenv
import os

load_dotenv()

API_KEY = os.getenv("API_KEY")  # Your SerpAPI key
GOOGLE_API_KEY = os.getenv("GOOGLE_API_KEY")
#PLACE_ID = "ChIJN1t_tDeuEmsRUsoyG83frY4"  # Replace with real Place ID

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

placeID = get_place_id(PLACE_NAME, LOCATION, GOOGLE_API_KEY)

url = f"https://serpapi.com/search.json?engine=google_maps_reviews&place_id={placeID}&api_key={API_KEY}"


response = requests.get(url)
data = response.json()

print(len(data['reviews']))

for review in data.get("reviews", []):
    print(f"👤 {review['user']['name']}: {review['snippet']} ⭐ {review['rating']}/5")


