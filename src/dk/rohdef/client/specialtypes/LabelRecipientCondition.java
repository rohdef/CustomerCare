package dk.rohdef.client.specialtypes;

import dk.rohdef.viewmodel.LabelRecipient;

/**
 * Select condition for LabelRecipients. 
 * @author Rohde Fischer <rohdef@rohdef.dk>
 */
public class LabelRecipientCondition implements RecipeintCondition<LabelRecipient> {
	public enum LabelCondition { ALL, COMPANY, CONTACT };
	
	private LabelCondition condition;

	/**
	 * Creates an LabelRecipentCondition that can selects items on various parameters. 
	 * The available options for this is :
	 * <ul>
	 * <li> ALL - All the possible recipients </li>
	 * <li> COMPANY - Accepts companies (determined by the name being "Virksomheden") </li>
	 * <li> CONTACTS - Accepts contacts (determined by the name not being "Virksomheden") </li>
	 * </ul>
	 * @param condition
	 */
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
