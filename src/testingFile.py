import requests

API_KEY = "fb478d6d113d758c063ec6b249094f5db10a6ec99f2d10f6583a220f6faf7199"
GOOGLE_API_KEY = "AIzaSyBzkUi-CSSBdrXY7vJmU3vgNYYm-iGl3PM"
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


