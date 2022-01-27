package in.nareshit.raghu.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import in.nareshit.raghu.consts.PurchaseOrderStatus;
import in.nareshit.raghu.model.PurchaseDtl;
import in.nareshit.raghu.model.PurchaseOrder;
import in.nareshit.raghu.service.IPartService;
import in.nareshit.raghu.service.IPurchaseOrderService;
import in.nareshit.raghu.service.IShipmentTypeService;
import in.nareshit.raghu.service.IWhUserTypeService;
import in.nareshit.raghu.view.VendorInvoicePdfView;

@Controller
@RequestMapping("/po")
public class PurchaseOrderController {

	@Autowired
	private IPurchaseOrderService service;

	@Autowired
	private IShipmentTypeService shipmentTypeService;

	@Autowired
	private IWhUserTypeService whUserTypeService;

	@Autowired
	private IPartService partService;


	/***
	 * For dynamic DropDown--screen#1
	 */
	private void addCommonUi(Model model) {
		model.addAttribute("sts", shipmentTypeService.getShipmentIdAndCodeByEnable("Yes"));
		model.addAttribute("vendors", whUserTypeService.getWhUserIdAndCodeByType("Vendor"));
	}


	//1. show Register
	@GetMapping("/register")
	public String showReg(Model model) {
		addCommonUi(model);
		return "PurchaseOrderRegister";
	}

	//2. create order
	@PostMapping("/save")
	public String save(
			@ModelAttribute PurchaseOrder purchaseOrder,
			Model model) 
	{
		try {
			Integer id = service.savePurchaseOrder(purchaseOrder);
			model.addAttribute("message", "Order '"+id+"' is created!");
		} catch (Exception e) {
			model.addAttribute("message", "Order is failed to created!");
			e.printStackTrace();
		}
		addCommonUi(model);
		return "PurchaseOrderRegister";
	}

	//3. display all
	@GetMapping("/all")
	public String getAll(Model model)
	{
		try {
			List<PurchaseOrder> list =  service.getAllPurchaseOrders();
			model.addAttribute("list", list);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "PurchaseOrderData";
	}

	///---------------------------(Screen#2)------------------------------
	private void commonUiForParts(Model model) {
		model.addAttribute("parts", partService.getPartIdAndCode());
	}

	// /po/parts?id=1
	@GetMapping("/parts")
	public String showPoPartsPage(
			@RequestParam Integer id, //PO ID /orderId
			Model model
			) 
	{
		// Load PO from DB, send PO to UI
		PurchaseOrder po = service.getOnePurchaseOrder(id);
		model.addAttribute("po", po);

		String status = service.getCurrentStatusOfPo(id);
		if(PurchaseOrderStatus.OPEN.name().equals(status) || 
				PurchaseOrderStatus.PICKING.name().equals(status)
				) 
		{
			// DropDown for Parts
			commonUiForParts(model);

		}

		//fetch all (if added parts)
		List<PurchaseDtl> poDtls = service.getPurchaseDtlsByPoId(id); //OrderId/POId
		model.addAttribute("list", poDtls);

		return "PurchaseOrderParts";
	}
	/**
	 * On Click Part Add, Read Form data as Dtl object
	 * save using service.
	 * Redirect to same page using /parts?id=<PurchaseOrderId>
	 * 
	 */
	@PostMapping("/addPart")
	public String addPart(PurchaseDtl dtl) {
		Integer poId = dtl.getPo().getId(); //PO Id/OrderId
		if( PurchaseOrderStatus.OPEN.name()
				.equals(service.getCurrentStatusOfPo(poId))
				|| 
				PurchaseOrderStatus.PICKING.name()
				.equals(service.getCurrentStatusOfPo(poId)) 
				) 
		{
			Integer partId = dtl.getPart().getId(); // partId

			Optional<PurchaseDtl> opt = service.getPurchaseDtlByPartIdAndPoId(partId, poId);
			if(opt.isPresent()) { //part already added update qty
				//..update call //(Qty, dtlId)
				service.updatePurchaseDtlQtyByDtlId(dtl.getQty(), opt.get().getId());
			} else { //part is adding first time to PO , so insert
				service.savePurchaseDtl(dtl);
			}

			if(PurchaseOrderStatus.OPEN.name()
					.equals(service.getCurrentStatusOfPo(poId))
					) 
			{
				service.updatePoStatus(poId, PurchaseOrderStatus.PICKING.name());
			}
		}
		return "redirect:parts?id="+ poId;
	}

	/***
	 * On Click Remove, delete one PurchaseDtl by dtlId.
	 * Redirect to 'PurchaseOrderParts' page using Path : /parts?id=<POorderId>
	 */
	// .... /removePart?poId=1&dtlId=30
	@GetMapping("/removePart")
	public String removePart(
			@RequestParam Integer poId,
			@RequestParam Integer dtlId
			)
	{
		if(PurchaseOrderStatus.PICKING.name()
				.equals(service.getCurrentStatusOfPo(poId))
				) 
		{
			service.deletePurchaseDtl(dtlId);
			if(service.getPurchaseDtlsCountByPoId(poId)==0) {
				service.updatePoStatus(poId, PurchaseOrderStatus.OPEN.name());
			}
		}
		return "redirect:parts?id="+poId; //orerId/PO ID
	}

	/***
	 * IncreaseQty by +1
	 * 
	 */
	@GetMapping("/increaseQty")
	public String increaseQty(
			@RequestParam Integer poId,
			@RequestParam Integer dtlId
			)
	{
		service.updatePurchaseDtlQtyByDtlId(1, dtlId);
		return "redirect:parts?id="+poId; //orerId/PO ID;
	}
	/***
	 * reduce Qty by -1
	 */
	@GetMapping("/reduceQty")
	public String reduceQty(
			@RequestParam Integer poId,
			@RequestParam Integer dtlId
			)
	{
		service.updatePurchaseDtlQtyByDtlId(-1, dtlId);
		return "redirect:parts?id="+poId; //orerId/PO ID;
	}

	/***
	 * PLACE ORDER
	 */
	@GetMapping("/placeOrder")
	public String placeOrder(@RequestParam Integer poId) 
	{
		if(PurchaseOrderStatus.PICKING.name()
				.equals(service.getCurrentStatusOfPo(poId))
				) 
		{
			service.updatePoStatus(poId, PurchaseOrderStatus.ORDERED.name());
		}
		return "redirect:parts?id="+poId;
	}
	/**
	 * CANCEL ORDER
	 */
	@GetMapping("/cancel")
	public String cancelOrder(@RequestParam Integer id) 
	{
		//only if current status is OPEN | PICKING | OREDRED | !CANCELLED
		String status = service.getCurrentStatusOfPo(id);
		if(
				PurchaseOrderStatus.PICKING.name().equals(status) ||
				PurchaseOrderStatus.ORDERED.name().equals(status) ||
				PurchaseOrderStatus.OPEN.name().equals(status)  ||
				!PurchaseOrderStatus.CANCELLED.name().equals(status)  
				) 
		{
			service.updatePoStatus(id, PurchaseOrderStatus.CANCELLED.name());
		}
		return "redirect:all";
	}
	
	
	/**
	 * GENERATE ORDER
	 */
	@GetMapping("/generate")
	public String generateInvoice(@RequestParam Integer id) 
	{
		service.updatePoStatus(id, PurchaseOrderStatus.INVOICED.name());
		return "redirect:all";
	}
	
	/***
	 * PDF Export
	 */
	@GetMapping("/print")
	public ModelAndView showVendorInvoice(
			@RequestParam Integer id)
	{
		ModelAndView m = new ModelAndView();
		m.setView(new VendorInvoicePdfView());
		
		List<PurchaseDtl> list = service.getPurchaseDtlsByPoId(id);
		m.addObject("list", list);
		
		PurchaseOrder po =  service.getOnePurchaseOrder(id);
		m.addObject("po", po);
		return m;
	}
}
