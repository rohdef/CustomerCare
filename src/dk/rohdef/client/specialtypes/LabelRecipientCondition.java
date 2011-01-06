package dk.rohdef.client.specialtypes;

import dk.rohdef.viewmodel.LabelRecipient;

public class LabelRecipientCondition implements RecipeintCondition<LabelRecipient> {
	public enum LabelCondition { ALL, COMPANY, CONTACT };
	
	private LabelCondition condition;
	
	public LabelRecipientCondition(LabelCondition condition) {
		this.condition = condition;
	}
	
	public boolean includeThis(LabelRecipient model) {
		if (condition == LabelCondition.ALL) {
			return true;
		} else if (condition == LabelCondition.COMPANY) {
			return model.getName().equals("Virksomheden");
		} else if (condition == LabelCondition.CONTACT) {
			return !model.getName().equals("Virksomheden");
		} else {
			throw new IllegalArgumentException("The provided LabelCondition isn't valid.");
		}
	}

}
