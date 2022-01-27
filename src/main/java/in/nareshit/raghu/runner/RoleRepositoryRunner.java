package in.nareshit.raghu.runner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import in.nareshit.raghu.consts.RoleType;
import in.nareshit.raghu.model.Role;
import in.nareshit.raghu.repo.RoleRepository;

/***
 * This class is executed only once
 * on application startup.
 * 
 */
@Component
@Order(10)
public class RoleRepositoryRunner implements CommandLineRunner {

	private static final Logger LOG = LoggerFactory.getLogger(RoleRepositoryRunner.class);
	
	@Autowired
	private RoleRepository repo;
	
	public void run(String... args) throws Exception {
		
		RoleType[] roleType = RoleType.values();
		
		for (RoleType rt : roleType) {
			if(!repo.existsByRole(rt)) {
				Role role = new Role();
				role.setRole(rt);
				repo.save(role);
				LOG.info(rt.name() + " (ROLE DATA) IS INSERTED INTO ROLE TABLE");
			}
		}
		
	}

}
