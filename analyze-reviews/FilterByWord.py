import json
from collections import Counter
from multiprocessing import Pool
from os import cpu_count
import time

import nltk
from nltk.corpus import stopwords
import pandas as pd
import re
import matplotlib.pyplot as plt
from transformers import pipeline
from nltk.tokenize import word_tokenize

noises = [".", ",", "?", ")", "(", ":", ";", "_", "-", " ́", "*", "!", "[", "]", "{", "}", "@", "%"]

nltk.download("stopwords")
stop_words = set(stopwords.words("english"))

escaped_noises = [re.escape(noise) for noise in noises]
noise_pattern = "|".join(escaped_noises)


def clean(text):
    text = re.sub(noise_pattern, "", text.lower())
    words = word_tokenize(text)  # Tokenizar
    words = [word for word in words if word not in stop_words and word.isalpha()]  # Filtrar stopwords y palabras no alfabéticas
    return words

def associated_words_by_review(word, review, co_occurrence_words):
    words = clean(review)

    if word in words:
        for i, w in enumerate(words):
            if word == w:
                context_words = words[max(0, i - 3):i] + words[i + 1:min(len(words), i + 3)]
                co_occurrence_words.update(context_words)

    return co_occurrence_words

def find_associated_words(word, reviews):
    co_occurrence_words = Counter()


    for review in reviews.values():
        co_occurrence_words = associated_words_by_review(word, review, co_occurrence_words)
    co_occurrence_words = Counter(co_occurrence_words)

    print(f"Words Counter: {co_occurrence_words}")
    most_common_words = co_occurrence_words.most_common(10)
    print(f"Most common words: {most_common_words}")
    most_common_dict = {word: count for word, count in most_common_words}
    print(f"Most common dict {most_common_dict}")

    return most_common_dict





