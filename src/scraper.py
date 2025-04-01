# -*- coding: utf-8 -*-
from googlemaps import GoogleMapsScraper
from datetime import datetime, timedelta
import argparse
import csv
from termcolor import colored
import time


ind = {'most_relevant' : 0 , 'newest' : 1, 'highest_rating' : 2, 'lowest_rating' : 3 }
HEADER = ['id_review', 'caption', 'relative_date', 'retrieval_date', 'rating', 'username', 'n_review_user', 'n_photo_user', 'url_user']
HEADER_W_SOURCE = ['id_review', 'caption', 'relative_date','retrieval_date', 'rating', 'username', 'n_review_user', 'n_photo_user', 'url_user', 'url_source']
URL = 'https://www.google.com/maps/place/Yugo+Melbourn+Point+-+Cork+Student+Accommodation/@51.8879257,-8.535235,17z/data=!3m1!4b1!4m6!3m5!1s0x48449182ecd7bf3f:0x973ce1eed05f526d!8m2!3d51.8879257!4d-8.5326601!16s%2Fg%2F11nyqz7rvp?entry=ttu&g_ep=EgoyMDI1MDMxMi4wIKXMDSoASAFQAw%3D%3D'
'''
def csv_writer(source_field, ind_sort_by, path='data/'):
    outfile= ind_sort_by + '_gm_reviews.csv'
    targetfile = open(path + outfile, mode='w', encoding='utf-8', newline='\n')
    writer = csv.writer(targetfile, quoting=csv.QUOTE_MINIMAL)

    if source_field:
        h = HEADER_W_SOURCE
    else:
        h = HEADER
    writer.writerow(h)

    return writer
'''

def getReviews(url, numberOfReviews=100):
    with GoogleMapsScraper() as scraper:
        error = scraper.sort_by(url, ind['newest'])

        if error == 0:

            n = 0
            while n < numberOfReviews:

                # logging to std out
                print(colored('[Review ' + str(n) + ']', 'cyan'))

                reviews = scraper.get_reviews(n)
                if len(reviews) == 0:
                    break

                for r in reviews:
                    print(r['username'], ": ", r['caption'])

                n += len(reviews)

url = "https://www.google.com/maps/place/Yugo+Melbourn+Point+-+Cork+Student+Accommodation/@51.8879257,-8.535235,17z/data=!4m8!3m7!1s0x48449182ecd7bf3f:0x973ce1eed05f526d!8m2!3d51.8879257!4d-8.5326601!9m1!1b1!16s%2Fg%2F11nyqz7rvp?entry=ttu&g_ep=EgoyMDI1MDMxMi4wIKXMDSoASAFQAw%3D%3D"
getReviews(url)

'''
if __name__ == '__main__':
    url = "https://www.google.com/maps/place/Yugo+Melbourn+Point+-+Cork+Student+Accommodation/@51.8879257,-8.535235,17z/data=!3m1!4b1!4m6!3m5!1s0x48449182ecd7bf3f:0x973ce1eed05f526d!8m2!3d51.8879257!4d-8.5326601!16s%2Fg%2F11nyqz7rvp?entry=ttu&g_ep=EgoyMDI1MDMxMi4wIKXMDSoASAFQAw%3D%3D"
    parser = argparse.ArgumentParser(description='Google Maps reviews scraper.')
    parser.add_argument('--N', type=int, default=100, help='Number of reviews to scrape')
    parser.add_argument('--i', type=str, default=URL, help='target URLs file')
    parser.add_argument('--sort_by', type=str, default='newest', help='most_relevant, newest, highest_rating or lowest_rating')
    #parser.set_defaults(place=False, debug=False, source=False)

    args = parser.parse_args()

    # store reviews in CSV file
    #writer = csv_writer(args.source, args.sort_by)
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
                    print(r['username'], ": ", r['caption'])

                n += len(reviews)
'''
