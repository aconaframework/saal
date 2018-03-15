package at.tuwien.ict.miklas.mind.aconaconnector;

import java.util.ArrayList;

import entity.mind.ExternalPerceptionInterface;
import evaluator.EvaluatorMindInterface;

public interface AconaMindGateway {
	public void setSensorInputs(ArrayList<ExternalPerceptionInterface> perception, double healthlevel, double healthChange, EvaluatorMindInterface score);
	public String getAction();
}
