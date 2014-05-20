package core;

import com.actix.rules.flow.nodes.Action;
import com.actix.rules.flow.nodes.Check;
import com.actix.rules.flow.nodes.Loop;

public class Branch {

	private Check beginCheck = null;
	private Action beginAction = null;
	private Loop beginLoop = null;

	private Check endCheck = null;
	private Action endAction = null;
	private Loop endLoop = null;
	
	public Branch(Check beginCheck, Check endCheck) {
		this.beginCheck = beginCheck;
		this.endCheck = endCheck;
	}

	public Branch(Check beginCheck, Action endAction) {
		this.beginCheck = beginCheck;
		this.endAction = endAction;
	}
	
	public Branch(Check beginCheck, Loop endLoop) {
		this.beginCheck = beginCheck;
		this.endLoop = endLoop;
	}

	public Branch(Action beginAction, Check endCheck) {
		this.beginAction = beginAction;
		this.endCheck = endCheck;
	}

	public Branch(Action beginAction, Action endAction) {
		this.beginAction = beginAction;
		this.endAction = endAction;
	}

	public Branch(Action beginAction, Loop endLoop) {
		this.beginAction = beginAction;
		this.endLoop = endLoop;
	}
	
	public Branch(Loop beginLoop, Check endCheck) {
		this.beginLoop = beginLoop;
		this.endCheck = endCheck;
	}

	public Branch(Loop beginLoop, Action endAction) {
		this.beginLoop = beginLoop;
		this.endAction = endAction;
	}
	
	public Branch(Loop beginLoop, Loop endLoop) {
		this.beginLoop = beginLoop;
		this.endLoop = endLoop;
	}


	public Check getBeginCheck() {
		return beginCheck;
	}

	public Check getEndCheck() {
		return endCheck;
	}

	public Action getBeginAction() {
		return beginAction;
	}

	public Action getEndAction() {
		return endAction;
	}

	public Loop getBeginLoop() {
		return beginLoop;
	}

	public Loop getEndLoop() {
		return endLoop;
	}
	
	public void setBeginCheck(Check beginCheck) {
		this.beginCheck = beginCheck;
	}

	public void setBeginLoop(Loop beginLoop) {
		this.beginLoop = beginLoop;
	}

	public void setEndCheck(Check endCheck) {
		this.endCheck = endCheck;
	}

	public void setEndLoop(Loop endLoop) {
		this.endLoop = endLoop;
	}
	
	
}
