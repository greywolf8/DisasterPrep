import requests

# Replace with your actual OpenRouter API key
API_KEY = "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"

url = "https://openrouter.ai/api/v1/chat/completions"

headers = {
    "Authorization": f"Bearer {API_KEY}",
    "Content-Type": "application/json"
}

data = {
    "model": "",
    "messages": [
        {"role": "system", "content": "You are a helpful assistant."},
        {"role": "user", "content": "Hello, can you confirm if my API key works?"}
    ]
}

response = requests.post(url, headers=headers, json=data)

if response.status_code == 200:
    print("✅ API key is working!")
    print("Response:", response.json()["choices"][0]["message"]["content"])
else:
    print("❌ Something went wrong:", response.status_code, response.text)