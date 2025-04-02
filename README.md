
# 📊 Business Reviews Analyzer
[![Contributors][contributors-shield]][contributors-url] [![Forks][forks-shield]][forks-url][![Starts][stars-shield]][stars-url]

## Introduction

Busines Reviews Analyzer is an open source application that retrieves and analyzes that retrieves and analyzes customer reviews. Using AI, it extracts insights, determines sentiment and identifies keywords related to specific aspects of a business or other usefull features.

## 🚀 Features

- Extracts reviews from a given Google business page URL.
- Analyzes sentiment (positive, neutral, negative) using AI.
- Allows users to search for a specific word in reviews.
- Displays insights about the keyword sentiment and frequently associated words or other features.
  
## Instalations

1. **Clone the Repository**
   
  ```sh
  git clone https://github.com/Carlos19uji/ReviewAnalyzer
  ```

2. **Navigate to the Project Directory**

    ```sh 
    cd ReviewAnalyzer
    ```
3. **Install Dependencies**

    ```sh
    pip install -r requirements.txt
    ```
    
4. **Run the Application**

   ```sh
    python main.py
    ```
   
## 📖 Usage

1. Enter the URL of a Google business page.
   
2. Click "Analyze to retrieve and process the reviews"

3. (Optional) Enter a keyword  (e.g., "room") to get insights related to that word.

4. View the AI-generated summary and insights

## 💻 Technologies Used

- Python
- Kotlin
  
## 🤝 Contributing

We welcome contributins! To contribute:

1. Fork the repository
2. Create a new branch
   git checkout -b feature-branch
3. Commit your changes
   git commit -m "Add new feature"
4. Push to your branch
   git push origin feature-branch
5. Open a Pull Request

## Used Models

This project uses machine learning models from Hugging Face:

- **[DistilBERT-base-uncased-finetuned-SST-2](https://huggingface.co/distilbert-base-uncased-finetuned-sst-2-english)**  
  - **License:** Apache 2.0  
  - **Description:** Model based on DistilBERT for sentiment analysis.  

- **[RoBERTa-base-go_emotions](https://huggingface.co/SamLowe/roberta-base-go_emotions)**  
  - **License:** MIT  
  - **Description:** Model based on RoBERTa for emotion classification.

## 📜 License

The **source code** is available under the **GPL 3.0 License** (see [LICENSE](LICENSE.md)).

### Notes on Licenses:
This project is distributed under the **GPL 3.0 license**. However, the mentioned models have their own licenses (Apache 2.0 and MIT), which must be respected when using this project.

## Acknoledgements
We would like to express our gratitude to everyone who contributed to this project.

A special thanks to [Mattia Gasparini](https://github.com/gaspa93) and his community for their work on [Google Maps Scraper](https://github.com/gaspa93/googlemaps-scraper), which served as an inspiration and foundation for this project. 

If you’d like to contribute, check out our [CONTRIBUTING.md](CONTRIBUTING.md) and join us in building something great!




[contributors-shield]: https://img.shields.io/github/contributors/Carlos19uji/review-analyzer
[contributors-url]: https://github.com/Carlos19uji/review-analyzer/graphs/contributors
[forks-shield]: https://img.shields.io/github/forks/Carlos19uji/review-analyzer
[forks-url]: https://github.com/Carlos19uji/review-analyzer/forks
[stars-shield]: https://img.shields.io/github/stars/Carlos19uji/review-analyzer
[stars-url]: https://github.com/Carlos19uji/review-analyzer/stargazers


