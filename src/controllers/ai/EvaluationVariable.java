package controllers.ai;

import gamecore.Coords;

public class EvaluationVariable
{
	public Coords coords;
	public int evaluationScore;

	EvaluationVariable()
	{
	}

	public EvaluationVariable(Coords coords, int evaluationScore) {
		this.coords = coords;
		this.evaluationScore = evaluationScore;
	}
}
