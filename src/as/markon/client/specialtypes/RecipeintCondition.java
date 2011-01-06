package as.markon.client.specialtypes;

import com.extjs.gxt.ui.client.data.BaseModel;

public interface RecipeintCondition<E extends BaseModel> {
	public boolean includeThis(E model);
}
