from flask import Flask, jsonify, request
import json
from AnalyzeReviews import process_reviews, count_words, common_words
from FilterByWord import find_associated_words

app = Flask(__name__)

@app.route("/results", methods=["GET"])
def get_results():
    analysis_results = process_reviews("hotel_corto.csv")
    common_words = count_words("hotel_corto.csv")

    print(jsonify({"analysis_results": analysis_results, "common_words": common_words}))
    return jsonify({"analysis_results": analysis_results, "common_words": common_words})


@app.route("/filter_by_word", methods=["POST"])
def filter_by_word():
    word = request.args.get("word")
    if not word:
        return jsonify({"error": "Missing 'word' parameter"}), 400

    reviews = request.json
    print(f"reviews: {reviews}")
    if not reviews:
        return jsonify({"error": "Missing 'review' parameter"}), 401

    related_words = find_associated_words(word, reviews)

    response = jsonify({"related_words": related_words})

    print(f"Response in json {response.get_json()}")
    return response

@app.route("/get_common_words", methods=["POST"])
def get_common_words():

    reviews = request.json
    print(f"reviews: {reviews}")
    if not reviews:
        return jsonify({"error": "Missing 'review' parameter"}), 401

    related_words = common_words(reviews)

    response = jsonify({"related_words": related_words})

    print(f"Response in json {response.get_json()}")
    return response


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000, ssl_context="adhoc")