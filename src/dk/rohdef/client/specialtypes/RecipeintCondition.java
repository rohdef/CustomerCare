package dk.rohdef.client.specialtypes;

import com.extjs.gxt.ui.client.data.BaseModel;

/**
 * Used to add one or more select conditions for the recipient panels. Usage examples can 
 * be that you want to select all companies begging with 'c', then you implement this 
 * checks the condition and returns true or false accordingly. 
 * @author Rohde Fischer <rohdef@rohdef.dk>
 *
 * @param <E>
 */
public interface RecipeintCondition<E extends BaseModel> {
	/**
	 * Checks if the model should be included in the selection depending on the chosen 
	 * selection parameters.
	 * @param model
	 * @return true if this model item should be included in the selection.
	 */
	public boolean includeThis(E model);
}
