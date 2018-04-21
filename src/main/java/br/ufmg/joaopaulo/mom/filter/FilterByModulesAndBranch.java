package br.ufmg.joaopaulo.mom.filter;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.filter.commit.CommitFilter;

public class FilterByModulesAndBranch implements CommitFilter {

	private String[] modules;
	
	public FilterByModulesAndBranch(String[] modules) {
		this.modules = modules;
	}
	
	@Override
	public boolean accept(Commit commit) {
		for (Modification modification : commit.getModifications()) {
			if (this.belongsToModules(modification.getNewPath())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean belongsToModules(String path) {
		for (String module : this.modules) {
			if (path.startsWith(module)) {
				return true;
			}
		}
		
		return false;
	}
	
}
