from nltk.corpus import stopwords
from collections import Counter
import json
import nltk
import pandas as pd
import re
from multiprocessing import Pool, cpu_count, Manager
from transformers import pipeline
from nltk.tokenize import word_tokenize
from Scraper import getReviews, ind, URL
from ObtainReviews import GoogleMapsScraper
import argparse
from termcolor import colored

noises = [".", ",", "?", ")", "(", ":", ";", "_", "-", " ́", "*", "!", "[", "]", "{", "}", "@", "%"]
nltk.download("stopwords")
stop_words = set(stopwords.words("english"))

escaped_noises = [re.escape(noise) for noise in noises]
noise_pattern = "|".join(escaped_noises)


def load_models(shared_dict):
    shared_dict["classifier"] = pipeline("sentiment-analysis", model="distilbert-base-uncased-finetuned-sst-2-english")
    shared_dict["emotion_classifier_extended"] = pipeline("text-classification", model="SamLowe/roberta-base-go_emotions")

reviews_map = {}

def split_text(text, max_length=256):
    tokens = word_tokenize(text)
    chunks = [tokens[i:i + max_length] for i in range(0, len(tokens), max_length)]
    return chunks


def analyze_sentiment(text, model):
    chunks = split_text(text)
    sentiments = [model(' '.join(chunk))[0]["label"] for chunk in chunks]

    sentiment_counts = {
        "POSITIVE": sentiments.count("POSITIVE"),
        "NEGATIVE": sentiments.count("NEGATIVE")
    }

    if sentiment_counts["POSITIVE"] > sentiment_counts["NEGATIVE"]:
        return "POSITIVE"
    elif sentiment_counts["NEGATIVE"] > sentiment_counts["POSITIVE"]:
        return "NEGATIVE"
    else:
        return "NEUTRAL"


def analyze_emotions(review, model):
    chunks = split_text(review)
    emotions_map = {}
    emotions_per_chunk = []


    for i, chunk in enumerate(chunks):
        emotion_data = model(chunk)
        emotion_map = {emotion['label']: emotion['score'] for emotion in emotion_data}
        emotions_map[f"chunk_{i+1}"] = emotion_map

        sorted_emotions = sorted(emotion_map.items(), key=lambda x: x[1], reverse=True)
        dominant_emotion, dominant_score = sorted_emotions[0]

        if dominant_emotion == "neutral":
            if len(sorted_emotions) > 1 and sorted_emotions[1][1] > 0.50:
                dominant_emotion = sorted_emotions[1][0]
            else:
                dominant_emotion = "neutral"
        emotions_per_chunk.append(dominant_emotion)

    emotion_counts = {emotion: emotions_per_chunk.count(emotion) for emotion in set(emotions_per_chunk)}
    print(f"Emotions map: {emotions_map}")
    print(f"Emotions per chunk: {emotions_per_chunk}")

    if len(emotion_counts) == 1:
        return list(emotion_counts.keys())[0]

    max_count = max(emotion_counts.values())
    dominant_emotions = [emotion for emotion, count in emotion_counts.items() if count == max_count]

    if len(dominant_emotions) == 1:
        return dominant_emotions[0]
    else:
        return "neutral"

def analyze_review(review_id, review_text, relative_date, rating, shared_dict):
    if not review_text.strip():
        return None, None

    clean_text = re.sub(noise_pattern, "", review_text.lower())

    sentiment = analyze_sentiment(clean_text, shared_dict["classifier"])
    emotion = analyze_emotions(clean_text, shared_dict["emotion_classifier_extended"])

    print(f"Review: {review_text}, Sentiment: {sentiment}, Emotion: {emotion}, Relative Date: {relative_date}, Rating: {rating}")
    return review_id, {"text": clean_text, "sentiment": sentiment, "emotion": emotion, "date": relative_date, "rating": rating}


def count_words(reviews_set):
    co_occurrence_counts = Counter()

    for review in reviews_set:
        text = review.get("caption", "")
        if not text:
            continue
        clean_text = re.sub(noise_pattern, "", text.lower())
        words = clean_text.split()
        filtered_words = [word for word in words if word not in stop_words]
        co_occurrence_counts.update(filtered_words)

    most_common_words = co_occurrence_counts.most_common(10)
    most_common_dict = {word: count for word, count in most_common_words}

    print(json.dumps(most_common_dict, indent=4))

    return most_common_dict

def common_words(reviews):
    co_occurrence_words = Counter()

    for review in reviews.values():
        clean_text = re.sub(noise_pattern, "", review.lower())
        words = clean_text.split()
        filtered_words = [word for word in words if word not in stop_words]
        co_occurrence_words.update(filtered_words)

    most_common_words = co_occurrence_words.most_common(10)
    most_common_dict = { word: count for word, count in most_common_words}

    return most_common_dict

def process_reviews(reviews_set):

    num_processes = cpu_count()
    print(f"Using {num_processes} processes in parallel")

    if not reviews_set:
        print("No reviews obtained")
        return {}

    with Manager() as manager:
        shared_dict = manager.dict()
        load_models(shared_dict)

        with Pool(num_processes) as pool:

            tasks = [
                (review.get("id_review", idx), review.get("caption", ""), review.get("relative_date", ""), review.get("rating", ""), shared_dict)
                for idx, review in enumerate(reviews_set)
                if review.get("caption") and review.get("caption").strip()
            ]

            results = pool.starmap(analyze_review, tasks)

        reviews_map = {review_id: data for review_id, data in results if review_id is not None}

    print(f"Reviews map: {reviews_map}")
    return reviews_map

if __name__ == '__main__':
    url = "https://www.google.com/maps/place/Yugo+Melbourn+Point+-+Cork+Student+Accommodation/@51.8879257,-8.535235,17z/data=!3m1!4b1!4m6!3m5!1s0x48449182ecd7bf3f:0x973ce1eed05f526d!8m2!3d51.8879257!4d-8.5326601!16s%2Fg%2F11nyqz7rvp?entry=ttu&g_ep=EgoyMDI1MDMxMi4wIKXMDSoASAFQAw%3D%3D"
    parser = argparse.ArgumentParser(description='Google Maps reviews scraper.')
    parser.add_argument('--N', type=int, default=100, help='Number of reviews to scrape')
    parser.add_argument('--i', type=str, default=URL, help='target URLs file')
    parser.add_argument('--sort_by', type=str, default='newest', help='most_relevant, newest, highest_rating or lowest_rating')

    args = parser.parse_args()

    # store reviews in CSV file
    url = "https://www.google.com/maps/place/Yugo+Melbourn+Point+-+Cork+Student+Accommodation/@51.8879257,-8.535235,17z/data=!4m8!3m7!1s0x48449182ecd7bf3f:0x973ce1eed05f526d!8m2!3d51.8879257!4d-8.5326601!9m1!1b1!16s%2Fg%2F11nyqz7rvp?entry=ttu&g_ep=EgoyMDI1MDMxMi4wIKXMDSoASAFQAw%3D%3D"
    with GoogleMapsScraper() as scraper:
        error = scraper.sort_by(url, ind[args.sort_by])

        if error == 0:

            n = 0
            while n < args.N:

                # logging to std out
                print(colored('[Review ' + str(n) + ']', 'cyan'))

                reviews = scraper.get_reviews(n)
                if len(reviews) == 0:
                    break

                for r in reviews:
                    print(f"Review obtained: {r}")

                n += len(reviews)

    reviews_set = getReviews(url, 1000)
    analyzed_reviews = process_reviews(reviews_set)
    common_words= count_words(reviews_set)
    print(json.dumps(analyzed_reviews, indent=2))

    print("\nPalabras más comunes:")
    print(f"Common Words: {common_words}")
