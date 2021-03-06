package in.nareshit.raghu.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import in.nareshit.raghu.exception.PartNotFoundException;
import in.nareshit.raghu.model.Part;
import in.nareshit.raghu.repo.PartRepository;
import in.nareshit.raghu.service.IPartService;
import in.nareshit.raghu.util.MyAppUtil;

@Service
public class PartServiceImpl implements IPartService {
	
	@Autowired
	private PartRepository repo;

	public Integer savePart(Part part) {
		return repo.save(part).getId();
	}

	public void updatePart(Part part) {
		repo.save(part);
	}

	public void deletePart(Integer id) {
		repo.delete(getOnePart(id));
	}

	
	public Part getOnePart(Integer id) {
		return repo.findById(id).orElseThrow(
				()->new PartNotFoundException("not exist")
				);
	}

	
	public List<Part> getAllParts() {
		return repo.findAll();
	}
	
	public Map<Integer, String> getPartIdAndCode() {
		List<Object[]> list = repo.getPartIdAndCode();
		return MyAppUtil.convertListToMap(list);
	}

}
