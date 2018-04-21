package br.ufmg.joaopaulo.mom;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.NoPersistence;
import org.repodriller.scm.CollectConfiguration;
import org.repodriller.scm.GitRepository;

import br.ufmg.joaopaulo.mom.entity.Responsible;
import br.ufmg.joaopaulo.mom.filter.FilterByModulesAndBranch;
import br.ufmg.joaopaulo.mom.util.SortUtil;

public class ModulesStudy implements Study {

	private String repositoryName;
	private String repositoryPath;
	private String[] modules;
	
	private static final double RESPONSIBLE_MINIMUM = 0.05;
	private static final double RESPONSIBLE_SUM = 0.8;
	
	public ModulesStudy(String repositoryName, String repositoryPath, String[] modules) {
		this.repositoryName = repositoryName;
		this.repositoryPath = repositoryPath;
		this.modules = modules;
	}
	
	public static void start(String[] args) {
		String repositoryName = args[0];
		String repositoryPath = args[1];
		
		String[] modules = new String[args.length - 2];
		for (int i = 2; i < args.length; i++) {
			modules[i - 2] = args[i];
		}
		
		new RepoDriller().start(new ModulesStudy(repositoryName, repositoryPath, modules));
	}
	
	@Override
	public void execute() {
		Map<String, List<Responsible>> responsiblesPerModule = new HashMap<String, List<Responsible>>();
		
		Map<String, Map<String, Integer>> committersPerModule = new HashMap<String, Map<String, Integer>>();
		Map<String, Integer> totalCommitsPerModule = new HashMap<String, Integer>();
		List<String> contributors = new ArrayList<String>();
		
		for (String module : this.modules) {
			Map<String, Integer> numberOfCommitsPerDeveloper = new HashMap<String, Integer>();
			committersPerModule.put(module, numberOfCommitsPerDeveloper);
			totalCommitsPerModule.put(module, 0);
		}
		
		new RepositoryMining()
			.in(GitRepository.singleProject(this.repositoryPath))
			.through(Commits.all())
			.filters(new OnlyNoMerge(), new FilterByModulesAndBranch(this.modules))
			.collect(new CollectConfiguration().basicOnly())
			.process(new ModulesVisitor(committersPerModule, totalCommitsPerModule, contributors), 
					new NoPersistence())
			.mine();
		
		SortUtil.sortUnderlyingMapsByValue(committersPerModule, false);
		
		Map<String, List<String>> modulesPerResponsible = new HashMap<String, List<String>>();
		
		for (Entry<String, Map<String, Integer>> committersPerModuleEntry : committersPerModule.entrySet()) {
			String module = committersPerModuleEntry.getKey();
			Map<String, Integer> numberOfCommitsPerDeveloper = committersPerModuleEntry.getValue();
			
			int totalCommitsForThisModule = totalCommitsPerModule.get(module);
			
			List<Responsible> responsibles = new ArrayList<Responsible>();
			
			double totalPercentage = 0;
			double lastPercentage = -1;
			
			for (Entry<String, Integer> numberOfCommitsPerDeveloperEntry : numberOfCommitsPerDeveloper.entrySet()) {
				String email = numberOfCommitsPerDeveloperEntry.getKey();
				Integer numberOfCommits = numberOfCommitsPerDeveloperEntry.getValue();
				
				double percentage = (double) numberOfCommits / totalCommitsForThisModule;
				
				if (lastPercentage != -1 && percentage != lastPercentage) {
					break;
				}
				
				if (percentage >= RESPONSIBLE_MINIMUM) {
					Responsible responsible = new Responsible();
					responsible.setEmail(email);
					responsible.setPercentage(percentage);
					responsibles.add(responsible);
					totalPercentage += percentage;
					
					if (modulesPerResponsible.get(email) == null) {
						modulesPerResponsible.put(email, new ArrayList<String>());
					}
					modulesPerResponsible.get(email).add(module);
				}
				
				if (totalPercentage >= RESPONSIBLE_SUM) {
					lastPercentage = percentage;
				}
			}
			
			responsiblesPerModule.put(module, responsibles);
		}
		
		System.out.println("---------------------------------");
		System.out.println("Responsibles per modules");
		System.out.println("---------------------------------");
		
		for (Entry<String, List<Responsible>> committersPerModuleEntry : responsiblesPerModule.entrySet()) {
			String module = committersPerModuleEntry.getKey();
			List<Responsible> responsibles = committersPerModuleEntry.getValue();
			
			System.out.println(module);
			
			for (Responsible responsible : responsibles) {
				String email = responsible.getEmail();
				double percentage = responsible.getPercentage();
				
				System.out.println(" - " + email + ": " + String.format("%.2f", percentage * 100) + "%");
			}
			
			System.out.println();
		}
		
		modulesPerResponsible = SortUtil.sortMapByTheSizeOfList(modulesPerResponsible, false);
		responsiblesPerModule = SortUtil.sortMapByTheSizeOfList(responsiblesPerModule, false);
		
		System.out.println("---------------------------------");
		System.out.println("Questions");
		System.out.println("---------------------------------");
		
		System.out.println("1. Number of contributors: " + contributors.size());
		System.out.println();
		
		System.out.println("2. Number of contributors that are responsibles for modules: " + 
				modulesPerResponsible.size());
		System.out.println();
		
		System.out.println("3. How many responsibles per module");
		
		for (Entry<String, List<Responsible>> mapEntry : responsiblesPerModule.entrySet()) {
			String module = mapEntry.getKey();
			List<Responsible> responsibles = mapEntry.getValue();
			System.out.println(" - " + module + ": " + responsibles.size());
		}
		
		System.out.println();
		
		System.out.println("4. How many modules per responsible");
		
		for (Entry<String, List<String>> mapEntry : modulesPerResponsible.entrySet()) {
			String responsible = mapEntry.getKey();
			List<String> modules = mapEntry.getValue();
			System.out.println(" - " + responsible + ": " + modules.size());
		}
		
		System.out.println();
		
		System.out.println("5. Modules Organization Matrix");
		
		this.saveModulesOrganizationMatrix(modulesPerResponsible, responsiblesPerModule);
		
		System.out.println("---------------------------------");
		System.out.println("End");
		System.out.println("---------------------------------");
	}
	
	private Responsible getResponsible(String email, List<Responsible> responsibles) {
		for (Responsible responsible : responsibles) {
			if (responsible.getEmail().equals(email)) {
				return responsible;
			}
		}
		return null;
	}
	
	private void saveModulesOrganizationMatrix(
			Map<String, List<String>> modulesPerResponsible, 
			Map<String, List<Responsible>> responsiblesPerModule) {
		modulesPerResponsible = SortUtil.sortMapByTheSizeOfList(modulesPerResponsible, false);
		responsiblesPerModule = SortUtil.sortMapByTheSizeOfList(responsiblesPerModule, true);
		
		PrintWriter printWriter = null;
		try {
			new File("matrices").mkdir();
			
			String fileName = "matrices/" + this.repositoryName + ".csv";
			Set<String> emails = modulesPerResponsible.keySet();
			
			File file = new File(fileName);
            file.setWritable(true);
            file.setReadable(true);
			FileWriter fileWriter = new FileWriter(file);
			printWriter = new PrintWriter(fileWriter);

			System.out.println("Developers: ");
			
			List<String> line = new ArrayList<String>();
			line.add("");
			
			int developersIndex = 1;
			for (String email : emails) {
				line.add("d" + developersIndex);
				System.out.println(" - d" + developersIndex + ": " + email);
				developersIndex++;
			}
			
			printWriter.println(StringUtils.join(line, ","));
			
			System.out.println();
			System.out.println("Modules: ");
			
			int modulesIndex = 1;
			for (Entry<String, List<Responsible>> entry : responsiblesPerModule.entrySet()) {
				String module = entry.getKey();
				List<Responsible> responsibles = entry.getValue();
				
				line = new ArrayList<String>();
				
				line.add("m" + modulesIndex);
				System.out.println(" - m" + modulesIndex + ": " + module);
				
				for (String email : emails) {
					Responsible responsible = this.getResponsible(email, responsibles);
					if (responsible != null) {
						line.add(String.format("%.2f", responsible.getPercentage() * 100));
					}
					else {
						line.add("");
					}
				}
				
				printWriter.println(StringUtils.join(line, ","));
				
				modulesIndex++;
			}
			
			System.out.println("Matrix written to file " + file.getAbsolutePath());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			printWriter.close();
		}
	}
	
}
