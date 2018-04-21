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
				Map<String, Integer> numberOfCommitsPerUser = this.committersPerModule.get(module);
				
				email = this.similarUser(email, module);
				
				if (numberOfCommitsPerUser.get(email) == null) {
					numberOfCommitsPerUser.put(email, 1);
				}
				else {
					numberOfCommitsPerUser.put(email, numberOfCommitsPerUser.get(email) + 1);					
				}
				
				this.totalCommitsPerModule.put(module, this.totalCommitsPerModule.get(module) + 1);
				
				if (!this.contributors.contains(email)) {
					this.contributors.add(email);
				}
			}
		}
	}
	
	private String similarUser(String email, String module) {
		String emailFirstPart = email.split("@")[0];
		String emailSecondPart = email.split("@")[1];
		
		String[] emailPointParts;
		if (emailFirstPart.contains(".")) {
			emailPointParts = emailFirstPart.split(".");	
		}
		else {
			emailPointParts = new String[] {emailFirstPart};
		}
		
		for (String user : this.contributors) {
			if (user.startsWith(emailFirstPart)) {
				return user;
			}
			
			String userFirstPart = user.split("@")[0];
			String userSecondPart = user.split("@")[1];
			
			if (emailSecondPart.equals(userSecondPart) && 
					(emailFirstPart.startsWith(userFirstPart) || userFirstPart.startsWith(emailFirstPart))) {
				return user;
			}
			
			String[] userPointParts;
			if (userFirstPart.contains(".")) {
				userPointParts = userFirstPart.split(".");				
			}
			else {
				userPointParts = new String[] {userFirstPart};
			}
			
			if (emailPointParts.length == userPointParts.length) {
				boolean equals = true;
				for (int i = 0; i < emailPointParts.length; i++) {
					if (!emailPointParts[i].equals(userPointParts[userPointParts.length - i - 1])) {
						equals = false;
						break;
					}
				}
				if (equals) {
					return user;
				}
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
