from pydantic import BaseModel, Field
from typing import Optional, List, Dict


class AnalyzeRequest(BaseModel):
    query: str


class Intent(BaseModel):
    type: str  # one of: category | score | search | source | nearby 
    intent_weight: float  # confidence score between 0 and 1
    entities: List[str]  # entities relevant to this intent
    locations: Optional[List[Dict[str, float]]] = Field(
        None, description="List of {lat, long} for nearby intent"
    )

    def get_locations(self) -> List[Dict[str, float]]:
        """
        Return the list of lat/long locations if type is 'nearby', else empty list.
        """
        if self.type == "nearby" and self.locations is not None:
            return self.locations
        return []


class AnalyzeResponse(BaseModel):
    intents: List[Intent]  # list of intents
    clarity_score: float  # overall clarity of the user query


class SummarizeRequest(BaseModel):
    title: str
    description: str
    url: Optional[str]


class SummarizeResponse(BaseModel):
    summary: str
