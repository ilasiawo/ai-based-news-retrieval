from fastapi import FastAPI, HTTPException
from app.models import AnalyzeRequest, AnalyzeResponse, SummarizeRequest, SummarizeResponse
from app.llm_utils import analyze_query, generate_catchy_summary
from app.langchain_loader import fetch_article_text

app = FastAPI()  # Create the app once

@app.post("/analyze", response_model=AnalyzeResponse)
async def analyze(req: AnalyzeRequest):
    try:
        result = await analyze_query(req.query)
        return AnalyzeResponse(**result)
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/summarize", response_model=SummarizeResponse)
async def summarize(req: SummarizeRequest):
    try:
        print(req)
        raw_text = ""
        if req.url:
            raw_text = await fetch_article_text(req.url)
        
        print(raw_text)
        summary = await generate_catchy_summary(
            req.title,
            req.description,
            raw_text or ""
        )

        return SummarizeResponse(summary=summary)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
