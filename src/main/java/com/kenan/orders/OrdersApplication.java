package com.kenan.orders;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

public class OrdersApplication {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrdersApplication.class);

	private static final BigDecimal stateTax = new BigDecimal(.063);
	private static final BigDecimal countyTax = new BigDecimal(.007);
	private static final BigDecimal cityTax = new BigDecimal(.02);
	private static Scanner sc = new Scanner(System.in);


	private static final Map<String, String[]> storeProducts = loadStoreProducts();

	public static void main(String[] args) {
		org.apache.log4j.BasicConfigurator.configure();

		String option = "";
		boolean orderFinished = true;
		boolean getOut = false;
		boolean paidInFull = true;
		
		List<String> shoppingCart = new ArrayList<>();

		while (true) {
			if (orderFinished && paidInFull) {
				mainMenu();
				orderFinished = false;
				paidInFull = false;
			}

			option = sc.nextLine().trim();

			switch (option) {

			case "reset":
				shoppingCart = new ArrayList<>();
				break;
			case "total":
				orderFinished = getOrderTotal(shoppingCart);
				break;
			case "exit":
				getOut = true;
				break;
			default:
				String foundItem = findItem(option);
				if (StringUtils.isNotBlank(foundItem)) {
					shoppingCart.add(foundItem);
				}
			}
			
			if(orderFinished) {
				
				paidInFull = true;
			}

			if (getOut) {
				break;
			}

		}

		sc.close();

	}

	private static String findItem(String productNumber) {

		String foundProduct = storeProducts.containsKey(productNumber) ? productNumber : "";

		if (StringUtils.isBlank(foundProduct)) {

			List<String> matchingProducts = new ArrayList<>();
			for (String s : storeProducts.keySet()) {
				String initial = s.substring(0, productNumber.length());
				if (StringUtils.equals(initial, productNumber)) {
					matchingProducts.add(s);
				}
			}
			if (matchingProducts.size() == 0) {
				System.out.println("Item doesn't match anything.");
			} else if (matchingProducts.size() == 1) {
				foundProduct = matchingProducts.get(0);
			} else {
				System.out.println("Found the following items that match the given product number.");
				for (String s : matchingProducts) {
					String[] product = storeProducts.get(s);
					System.out.println(s + " " + product[0]);
					
				}
				return "";
			}
		}
		String[] product = storeProducts.get(foundProduct);
		System.out.println(product[0] + " " + product[1]);
		return foundProduct;
	}

	/**
	 * Trusting the cashier to enter appropriate values for payments. 
	 * Future implementations would have data validations.
	 * @param shoppingCart
	 * @return
	 * @throws NumberFormatException
	 */
	private static boolean getOrderTotal(List<String> shoppingCart) throws NumberFormatException{
		BigDecimal total = new BigDecimal(0);
		BigDecimal taxTotal = new BigDecimal(0);
		BigDecimal subTotal = new BigDecimal(0);
		
		Map<String, BigDecimal> taxJurisdictions = new HashMap<>();
		Map<String, String> orderTotalsInformation = new HashMap<>();
		
		for (String productNum : shoppingCart) {
			String[] productInfo = storeProducts.get(productNum);
			BigDecimal[] totals = calculateItemValues(productInfo);
			
			subTotal = subTotal.add(totals[0]);
			taxTotal = taxTotal.add(totals[1]);
			total = total.add(totals[2]);
			String taxType = productInfo[productInfo.length - 1];
			if(taxJurisdictions.get(taxType) == null){
				taxJurisdictions.put(taxType, taxTotal);
			} else {
				taxJurisdictions.put(taxType, taxJurisdictions.get(taxType).add(taxTotal));
			}
		}
		System.out.println("Subtotal: " + subTotal.setScale(2, RoundingMode.UP).doubleValue());
		System.out.println("Tax Total: " + taxTotal.setScale(2, RoundingMode.UP).doubleValue());
		System.out.println("Total: " + total.setScale(2, RoundingMode.UP).doubleValue() + "\n");
		
		orderTotalsInformation.put("subtotal", subTotal.setScale(2, RoundingMode.UP).toString());
		orderTotalsInformation.put("taxTotal", taxTotal.setScale(2, RoundingMode.UP).toString());
		orderTotalsInformation.put("total", total.setScale(2, RoundingMode.UP).toString());
		
		System.out.print("Enter amount paid: ");
		BigDecimal amountPaid = new BigDecimal(sc.nextLine().trim());
		
		total = total.subtract(amountPaid);
		total = total.setScale(2, RoundingMode.UP);
		while(total.compareTo(BigDecimal.ZERO) > 1) {
			System.out.println("Remaining amount due: " + total.doubleValue());
			System.out.println("Enter additional payment amount: ");
			BigDecimal additionalPay = new BigDecimal(sc.nextLine().trim());
			amountPaid = amountPaid.add(additionalPay);
			total = total.subtract(additionalPay);
		}
		
		orderTotalsInformation.put("amountPaid", amountPaid.setScale(2, RoundingMode.UP).toString());
		
		if(total.compareTo(BigDecimal.ZERO) < 0) {
			total = total.negate();
		}
		orderTotalsInformation.put("change", total.toString());
		getOrderReceipt(taxJurisdictions, orderTotalsInformation, shoppingCart);
		return true;
	}
	
	private static void getOrderReceipt(Map<String, BigDecimal> taxJurisdiction, Map<String, String> orderTotalsInformation, List<String> shoppingCart) {
		for(String item : shoppingCart) {
			String [] thisItem = storeProducts.get(item);
			System.out.println(thisItem[0] + " " + item + " " + thisItem[1] + " " + thisItem[2]);
		}
		System.out.println("\n ---- Receipt ---- ");
		System.out.println("Subtotal: " + orderTotalsInformation.get("subtotal"));
		System.out.println("Tax Information");
		for(String s : taxJurisdiction.keySet()) {
			System.out.println(s + " " + taxJurisdiction.get(s).setScale(2, RoundingMode.UP).toString());
		}
		System.out.println("Total: " + orderTotalsInformation.get("total")); 
		System.out.println("Amount Paid: " + orderTotalsInformation.get("amountPaid"));
		System.out.println("Change: " + orderTotalsInformation.get("change") + "\n");
	}

	private static BigDecimal[] calculateItemValues(String[] productInfo) throws NumberFormatException{
		
		String category = productInfo[2];
		BigDecimal itemPrice = new BigDecimal(productInfo[1]);
		BigDecimal itemTax = new BigDecimal(0);
		BigDecimal itemTotal = new BigDecimal(0);
		switch (category) {
		case "g":
			itemTax = itemPrice.multiply(cityTax);
			itemTotal = itemTax.add(itemPrice);
			break;
		case "pf":
		case "pd":
		case "nd":
		case "c":
		case "o":
		default:
			itemTax = itemPrice.multiply(cityTax);
			itemTax = itemTax.add(itemPrice.multiply(countyTax));
			itemTax = itemTax.add(itemPrice.multiply(stateTax));
			itemTotal = itemTax.add(itemPrice);
			
		}
		return new BigDecimal[] {itemPrice, itemTax, itemTotal};
	}

	private static void mainMenu() {
		System.out.println("Enter product number to add item to cart.");
		System.out.println("Enter 'reset' to reset shopping cart.");
		System.out.println("Enter 'total' to get total bill.");
		System.out.println("Enter 'exit' to exit system.\n");
	}

	private static final Map<String, String[]> loadStoreProducts() {
		CSVReader reader = null;
		try {
			LOGGER.info("Loading data...");
			reader = new CSVReader(new FileReader("src/main/resources/product_info.csv"));
		} catch (FileNotFoundException fnf) {
			LOGGER.error("Failed to find the file with product information.", fnf.getMessage());
			System.exit(0);
		}

		Map<String, String[]> storeProducts = new HashMap<>();

		try {
			String[] currLine = reader.readNext();
			while (currLine != null) {
				String[] productDetails = new String[currLine.length - 1];
				for (int i = 0; i < productDetails.length; ++i) {
					productDetails[i] = currLine[i + 1].trim();
				}
				storeProducts.put(currLine[0].trim(), productDetails);
				currLine = reader.readNext();

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		LOGGER.info("Data is now loaded.");
		return storeProducts;
	}
}
