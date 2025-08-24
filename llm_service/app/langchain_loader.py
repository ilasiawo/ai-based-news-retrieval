import httpx
import trafilatura

async def fetch_article_text(url: str) -> str:
    headers = {
        "User-Agent": (
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/114.0 Safari/537.36"
        ),
        "Accept-Language": "en-US,en;q=0.9",
        "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
        "Referer": "https://www.google.com/",
        "Connection": "keep-alive"
    }

    try:
        async with httpx.AsyncClient(timeout=30.0, follow_redirects=True) as client:
            resp = await client.get(url, headers=headers)
            resp.raise_for_status()

        downloaded = trafilatura.extract(resp.text, include_comments=False, include_tables=False)
        if downloaded:
            print(downloaded.strip())
            return downloaded.strip()
        else:
            return "No main article text could be extracted."

    except httpx.HTTPStatusError as e:
        return f"HTTP error {e.response.status_code} for URL {url}"
    except httpx.RequestError as e:
        return f"Request error: {e}"
