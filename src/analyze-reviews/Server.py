from flask import Flask, jsonify, request
from AnalyzeReviews import process_reviews, count_words, common_words
from FilterByWord import find_associated_words
from Scraper import getReviews

app = Flask(__name__)

##https://www.google.com/maps/place/Yugo+Melbourn+Point+-+Cork+Student+Accommodation/@51.8879257,-8.535235,17z/data=!4m8!3m7!1s0x48449182ecd7bf3f:0x973ce1eed05f526d!8m2!3d51.8879257!4d-8.5326601!9m1!1b1!16s%2Fg%2F11nyqz7rvp?entry=ttu&g_ep=EgoyMDI1MDMxMi4wIKXMDSoASAFQAw%3D%3D##


@app.route("/results", methods=["POST"])
def get_results():

    url = request.args.get("url")
    reviews_set = getReviews(url, 1000)
    analysis_results = process_reviews(reviews_set)
    common_words = count_words(reviews_set)

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