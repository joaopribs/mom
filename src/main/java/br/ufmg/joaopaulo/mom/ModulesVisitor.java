package br.ufmg.joaopaulo.mom;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class ModulesVisitor implements CommitVisitor {

	private Map<String, Map<String, Integer>> committersPerModule;
	private Map<String, Integer> totalCommitsPerModule;
	private List<String> contributors;
	
	public ModulesVisitor(
			Map<String, Map<String, Integer>> committersPerModule, 
			Map<String, Integer> totalCommitsPerModule, 
			List<String> contributors) {
		this.committersPerModule = committersPerModule;
		this.totalCommitsPerModule = totalCommitsPerModule;
		this.contributors = contributors;
	}
	
	@Override
	public void process(SCMRepository repository, Commit commit, PersistenceMechanism writer) {
		for (Modification modification : commit.getModifications()) {
			String email = commit.getAuthor().getEmail();
			String filePath = modification.getNewPath();
			
			for (String module : this.modulesToWhichItBelongs(filePath)) {
				Map<String, Integer> numberOfCommitsPerDeveloper = this.committersPerModule.get(module);
				
				email = this.similarDeveloper(email, module);
				
				if (numberOfCommitsPerDeveloper.get(email) == null) {
					numberOfCommitsPerDeveloper.put(email, 1);
				}
				else {
					numberOfCommitsPerDeveloper.put(email, numberOfCommitsPerDeveloper.get(email) + 1);					
				}
				
				this.totalCommitsPerModule.put(module, this.totalCommitsPerModule.get(module) + 1);
				
				if (!this.contributors.contains(email)) {
					this.contributors.add(email);
				}
			}
		}
	}
	
	private String similarDeveloper(String email, String module) {
		String emailFirstPart = email.split("@")[0];
		
		for (String developerEmail : this.contributors) {
			String developerEmailFirstPart = developerEmail.split("@")[0];
			
			if (emailFirstPart.equals(developerEmailFirstPart)) {
				return developerEmail;
			}
		}
		
		return email;
	}
	
	private Set<String> modulesToWhichItBelongs(String path) {
		Set<String> modulesToWhichItBelongs = new HashSet<String>();
		
		for (String module : this.committersPerModule.keySet()) {
			if (path.startsWith(module)) {
				modulesToWhichItBelongs.add(module);
			}
		}
		
		return modulesToWhichItBelongs;
	}
}
