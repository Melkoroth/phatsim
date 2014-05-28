package phat.codeproc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

import ingenias.exception.NotFound;
import ingenias.exception.NullEntity;
import ingenias.generator.browser.Browser;
import ingenias.generator.browser.Graph;
import ingenias.generator.browser.GraphAttribute;
import ingenias.generator.browser.GraphEntity;
import ingenias.generator.datatemplate.Repeat;
import ingenias.generator.datatemplate.Sequences;
import ingenias.generator.datatemplate.Var;

public class ActivityGenerator {
	static final String ADL_SPEC_DIAGRAM = "ADLSpecDiagram";
	static final String ACTIVITY_DIAGRAM = "ActivityDiagram";
	static final String ACTIVITY_SPEC_FIELD = "ActivitySpecField";
	static final String ACTIVITY_TYPE = "BActivity";
	static final String NEXT_ACTIVITY_REL = "NextActivity";
	static final String TRUE_FLOW_REL = "TrueFlow";
	static final String FALSE_FLOW_REL = "FalseFlow";
	static final String IF_FLOW_CONTROL_TYPE = "IFFlowControl";
	static final String IF_FLOW_COND_REL = "cond";
	static final String SEQ_TASK_DIAGRAM_FIELD = "SeqTaskDiagramField";

	Browser browser;

	public ActivityGenerator(Browser browser) {
		this.browser = browser;
	}

	public void generateTimeIntervals(Sequences seq) throws NullEntity,
			NotFound {
		// System.out.println("generateTimeIntervals");
		generateActivityClasses(seq);
		generateTimeIntervalClasses(seq);

	}

	public void generateActivityClasses(Sequences seq) throws NullEntity,
			NotFound {
		for (GraphEntity ge : browser.getAllEntities()) {
			if (ge.getType().equals(ACTIVITY_TYPE)) {
				Repeat rep = new Repeat("activities");
				rep.add(new Var("actName", ge.getID()));
				seq.addRepeat(rep);

				GraphAttribute ga = ge
						.getAttributeByName(SEQ_TASK_DIAGRAM_FIELD);
				if (ga != null) {
					String actDiagId = ga.getSimpleValue();
					System.out.println(ge.getID() + " >-* " + actDiagId);
					if (actDiagId != null && !actDiagId.equals("")) {
						Repeat repSeq = new Repeat("seqTaskSpec");
						repSeq.add(new Var("seqTaskName", actDiagId));
						rep.add(repSeq);
					}
				}
			}
		}
	}

	private void generateTimeIntervalClasses(Sequences seq) throws NullEntity,
			NotFound {
		for (Graph diagram : Utils.getGraphsByType(ADL_SPEC_DIAGRAM, browser)) {
			System.out.println("ADLDiagramName: " + diagram.getID());
			for (GraphEntity ge : diagram.getEntities()) {
				if (ge.getType().equals("TimeInterval")) {
					// Define class structure
					Repeat rep = new Repeat("tis");
					rep.add(new Var("tisName", ge.getID()));
					seq.addRepeat(rep);
					GraphAttribute ga = ge
							.getAttributeByName(ACTIVITY_SPEC_FIELD);
					System.out.println("ACTIVITY_SPEC_FIELD (" + ge.getID()
							+ "): " + ga);
					if (ga != null) {
						String actDiagId = ga.getSimpleValue();
						if (actDiagId != null && !actDiagId.equals("")) {
							System.out.println("**" + actDiagId);
							Graph actDiagram = Utils.getGraphByName(actDiagId,
									browser);
							if (actDiagram != null) {
								initActivitiesAndDependences(actDiagram, rep);
							}
						} else {
							// No activities are specified
							Repeat repDoNothing = new Repeat("noActivities");
							rep.add(repDoNothing);
						}
					}
				}
			}
		}
	}

	private void initActivitiesAndDependences(Graph adlSpec, Repeat repFather)
			throws NotFound, NullEntity {

		GraphEntity ge = Utils.getFirstEntity(adlSpec);
		Repeat repFirst = new Repeat("firstActivity");
		repFather.add(repFirst);
		repFirst.add(new Var("actName", ge.getID()));

		for (GraphEntity activity : adlSpec.getEntities()) {
			System.out.println(">>>>entity ->" + activity.getType() + ":"
					+ activity.getID());
			if (activity.getType().equals(ACTIVITY_TYPE)) {
				String activityName = activity.getID();
				System.out.println("ActivityName = " + activityName);

				// Defines the activity
				Repeat rep = new Repeat("activities");
				repFather.add(rep);
				rep.add(new Var("actName", activityName));

				for (GraphEntity nextAct : Utils.getTargetsEntity(activity,
						NEXT_ACTIVITY_REL)) {
					if (nextAct.getType().equals(ACTIVITY_TYPE)) {
						// registers a transition between activities without any
						// condition
						Repeat rep2 = new Repeat("regTrans");
						repFather.add(rep2);
						rep2.add(new Var("actSource", activityName));
						rep2.add(new Var("actTarget", nextAct.getID()));
					}
				}
			} else if (activity.getType().equals(IF_FLOW_CONTROL_TYPE)) {
				// registers one or more transitions between activities
				// depending on the conditions
				processCondition(activity, repFather);
			}
		}
	}

	private GraphEntity processCondition(GraphEntity ifnode, Repeat repFather)
			throws NotFound {
		List<GraphEntity> sources = new ArrayList<GraphEntity>();
		sources.addAll(Utils.getSourcesEntity(ifnode, NEXT_ACTIVITY_REL));
		sources.addAll(Utils.getSourcesEntity(ifnode, TRUE_FLOW_REL));
		sources.addAll(Utils.getSourcesEntity(ifnode, FALSE_FLOW_REL));

		Collection<GraphEntity> conds = Utils.getTargetsEntity(ifnode,
				IF_FLOW_COND_REL);

		GraphEntity nextTrueGE = Utils.getTargetEntity(ifnode, TRUE_FLOW_REL);
		GraphEntity nextFalseGE = Utils.getTargetEntity(ifnode, FALSE_FLOW_REL);

		String condSentence = ConditionGenerator.generateCondition(conds);
                Repeat repCond = new Repeat("regCondTrans");
                repFather.add(repCond);
                repCond.add(new Var("condInst", condSentence));
		for (GraphEntity source : sources) {
			if (nextTrueGE != null) {
				Repeat rep = new Repeat("regTrueTrans");
				repCond.add(rep);
				rep.add(new Var("actSource", source.getID()));
				rep.add(new Var("actTarget", nextTrueGE.getID()));
			}
			if (nextFalseGE != null) {
				Repeat rep = new Repeat("regFalseTrans");
				repCond.add(rep);
				rep.add(new Var("actSource", source.getID()));
				rep.add(new Var("actTarget", nextFalseGE.getID()));
			}
		}
		return null;
	}
}
