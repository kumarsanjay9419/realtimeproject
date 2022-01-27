package in.nareshit.raghu.service;

//ctrl+shift+O
import java.util.List;
import java.util.Map;

import in.nareshit.raghu.model.ShipmentType;

public interface IShipmentTypeService {

	Integer saveShipmentType(ShipmentType st);
	void updateShipmentType(ShipmentType st);
	void deleteShipmentType(Integer id);
	
	List<ShipmentType>  getAllShipmentTypes();
	ShipmentType getShipmentType(Integer id);
	
	boolean isShipmentTypeCodeExist(String code);
	boolean isShipmentTypeCodeExistForEdit(String code,Integer id);
	
	List<Object[]> getShipmentTypeModeAndCount();
	
	//for Dynamic DropDown
	Map<Integer,String> getShipmentIdAndCodeByEnable(String enable);
}
