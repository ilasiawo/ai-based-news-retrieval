import os
import json
from dotenv import load_dotenv
from openai import AsyncOpenAI

load_dotenv()
api_key = os.getenv("OPENAI_API_KEY")

client = AsyncOpenAI(api_key=api_key)

async def call_openai(prompt: str, max_tokens: int = 512, temperature: float = 0.4) -> str:
    """
    Call OpenAI chat completion in async mode.
    """
    messages = [{"role": "user", "content": prompt}]
    response = await client.chat.completions.create(
        model="gpt-4o-mini",  # Cost-effective and performant
        messages=messages,
        max_tokens=max_tokens,
        temperature=temperature,
    )
    return response.choices[0].message.content.strip()

async def analyze_query(query: str):
    """
    Analyze the user news query to extract:
    - intents (list of intent objects with type, intent_weight, entities, optional latitude/longitude)
    - clarity_score (how clear/specific the ask is)

    Returns a JSON object matching the updated schema.
    """

    prompt = f"""
    You are an assistant that analyzes a user’s news-related query.
    Extract structured information and return valid JSON ONLY, matching the schema below.

    Schema:
    {{
      "intents": [
        {{
          "type": "one of: category | score | search | source | nearby",
          "intent_weight": 0.0,    // 0–1, confidence in this intent
          "entities": [ "list of entities corresponding to this intent (people, organizations, locations, events)" ],
          "locations": [            // optional, only if type is 'nearby'
            {{
              "lat": 0.0,
              "long": 0.0
            }}
          ]
        }}
      ],
      "clarity_score": 0.0       // 0–1, how clear/specific the query is
    }}

    Guidelines:
    - Always return valid JSON only, with no extra text.
    - Entities: include names of people, organizations, locations, or events explicitly mentioned for each intent.
    - Intents: include all likely intents ordered by confidence.
    - Intent weight: confidence in that intent (0–1).
    - Clarity score: measure how precise the user’s query is (0 = vague, 1 = very clear).
    - For nearby intents, location will be given as city name or landmark; convert to lat/long, and include locations list with lat and long as floats.
    - For nearby intents, 

    Query: {query}

    JSON:
    """

    # Call OpenAI asynchronously
    response = await call_openai(prompt, max_tokens=256)

    try:
        print(response)
        return json.loads(response)
    except json.JSONDecodeError:
        # Fallback if LLM returns invalid JSON
        return {"raw_response": response}


async def generate_catchy_summary(title: str, description: str, content: str) -> str:
    """
    Generate a catchy, concise summary (~100 words) of the news article.
    """
    prompt = (
        f"Write a concise and engaging news summary (~100 words) based on the below details:\n"
        f"Title: {title}\n"
        f"Description: {description}\n"
        f"Content: {content}\n"
    )
    prompt += "Summary:"
    return await call_openai(prompt, max_tokens=256)

# Optional: utility if you want to allow configurable temperature
DEFAULT_TEMPERATURE = 0.4
