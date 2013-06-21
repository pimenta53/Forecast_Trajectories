package ontology;

import jade.content.Predicate;

public class SupposedWay implements Predicate {

	private Way supposedlWay;
	private boolean end = false;
	// adicionar variavel que diz se esta numa zona determinada ou nao
	
	
	public Way getSupposedlWay() {
		return supposedlWay;
	}

	public boolean isEnd() {
		return end;
	}

	public void setEnd(boolean end) {
		this.end = end;
	}

	public void setSupposedlWay(Way supposedlWay) {
		this.supposedlWay = supposedlWay;
	}
	
}