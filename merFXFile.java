package fxOne;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.swing.Spring;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;

public class merFXFile {
	
	//globalsettings
	
	public String jnlpUrl = ""; 
	public String userName = ""; 
	public String password = "";  
	public String code = ""; 
	public String reportfile = ""; 	
	
	public String instrument = "";
	public String initialDeposit = ""; 
	public String leverage = ""; 
	public String marginCutLevel = ""; 
	
	public String info = "";
	public String pathfile = ""; 
	public String csvpathfile = "";
	public String reportpathfile = "";
	public String activeDB = "";
	public String activeCSV = ""; 
	public String activeGateway = "";
	
	public String enableGUI = "";
	public String activeFxOne  = ""; 
	public String dateFrom = "";
	public String dateTo = "";

	//strategyparams
	public String intParam = "";
	public String doubleParam = "";
	public String boolParam = "";
	public String textParam = "";
	public String file = "";
	public String currentTime = "";
	public String color = "";
	public String instrument2 = "";
	public String period = "";
	
	//iparameters
	public String name = "";
	public String amount = "";
	public String shift = "";
	public String iperiod = "";
	public String slPips = "";
	public String tpPipsOnLoss = "";
	public String tpPipsOnProfit = "";
	public String setCalendar = "";
	public String mode = "";
	public String enable = "";

  public merFXFile() {
	  
    try {

	File fXmlFile = new File("merFX.xml");
	DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
	Document doc = dBuilder.parse(fXmlFile);

	//optional, but recommended
	//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
	doc.getDocumentElement().normalize();

	System.out.println("Root element :" + doc.getDocumentElement().getNodeName());

	NodeList nList = doc.getElementsByTagName("globalsettings");

	System.out.println("----------------------------");

	for (int temp = 0; temp < nList.getLength(); temp++) {

		Node nNode = nList.item(temp);

		System.out.println("\nCurrent Element :" + nNode.getNodeName());

		if (nNode.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement = (Element) nNode;

			jnlpUrl = eElement.getElementsByTagName("jnlpUrl").item(0).getTextContent();
			userName = eElement.getElementsByTagName("userName").item(0).getTextContent();
			password = eElement.getElementsByTagName("password").item(0).getTextContent();
			code = eElement.getElementsByTagName("code").item(0).getTextContent();
			reportfile = eElement.getElementsByTagName("reportfile").item(0).getTextContent();
			
			instrument = eElement.getElementsByTagName("instrument").item(0).getTextContent();
			initialDeposit = eElement.getElementsByTagName("initialDeposit").item(0).getTextContent(); 
			
			leverage = eElement.getElementsByTagName("leverage").item(0).getTextContent(); 
			marginCutLevel = eElement.getElementsByTagName("marginCutLevel").item(0).getTextContent(); 
			
			info = eElement.getElementsByTagName("info").item(0).getTextContent();
			pathfile = eElement.getElementsByTagName("pathfile").item(0).getTextContent();
			csvpathfile = eElement.getElementsByTagName("csvpathfile").item(0).getTextContent();
			reportpathfile = eElement.getElementsByTagName("reportpathfile").item(0).getTextContent();
			
			activeDB = eElement.getElementsByTagName("activeDB").item(0).getTextContent();			
			activeCSV = eElement.getElementsByTagName("activeCSV").item(0).getTextContent();
			activeGateway = eElement.getElementsByTagName("activeGateway").item(0).getTextContent();
			
			enableGUI = eElement.getElementsByTagName("enableGUI").item(0).getTextContent();
			activeFxOne = eElement.getElementsByTagName("activeFxOne").item(0).getTextContent();
			dateFrom = eElement.getElementsByTagName("dateFrom").item(0).getTextContent();
			dateTo = eElement.getElementsByTagName("dateTo").item(0).getTextContent();
		}
	}
	
	outToConsole();
	
	NodeList nList2 = doc.getElementsByTagName("strategyparams");

	System.out.println("----------------------------");

	for (int temp = 0; temp < nList2.getLength(); temp++) {

		Node nNode2 = nList2.item(temp);

		System.out.println("\nCurrent Element :" + nNode2.getNodeName());

		if (nNode2.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement2 = (Element) nNode2;
			
			intParam = eElement2.getElementsByTagName("intParam").item(0).getTextContent();
			doubleParam = eElement2.getElementsByTagName("doubleParam").item(0).getTextContent();
			boolParam = eElement2.getElementsByTagName("boolParam").item(0).getTextContent();
			textParam = eElement2.getElementsByTagName("textParam").item(0).getTextContent();
			file = eElement2.getElementsByTagName("file").item(0).getTextContent();
			currentTime = eElement2.getElementsByTagName("currentTime").item(0).getTextContent();
			color = eElement2.getElementsByTagName("color").item(0).getTextContent();
			instrument2 = eElement2.getElementsByTagName("instrument").item(0).getTextContent();
			
			period = eElement2.getElementsByTagName("period").item(0).getTextContent();
			
		}
	}
	
	outToConsole2();
	
	NodeList nList3 = doc.getElementsByTagName("iparameters");
	
	System.out.println("----------------------------\n");
	
	for (int temp = 0; temp < nList3.getLength(); temp++) {

		Node nNode3 = nList3.item(temp);

		System.out.println("\nCurrent Element :" + nNode3.getNodeName());

		if (nNode3.getNodeType() == Node.ELEMENT_NODE) {

			Element eElement3 = (Element) nNode3;
			name = eElement3.getElementsByTagName("name").item(0).getTextContent();
			amount = eElement3.getElementsByTagName("amount").item(0).getTextContent();
			shift = eElement3.getElementsByTagName("shift").item(0).getTextContent();
			iperiod = eElement3.getElementsByTagName("iperiod").item(0).getTextContent();
			slPips = eElement3.getElementsByTagName("slPips").item(0).getTextContent();
			tpPipsOnLoss = eElement3.getElementsByTagName("tpPipsOnLoss").item(0).getTextContent();
			tpPipsOnProfit = eElement3.getElementsByTagName("tpPipsOnProfit").item(0).getTextContent();
			setCalendar = eElement3.getElementsByTagName("setCalendar").item(0).getTextContent();
			mode = eElement3.getElementsByTagName("mode").item(0).getTextContent();		
			enable = eElement3.getElementsByTagName("enable").item(0).getTextContent();	
		}
	}	
	outToConsole3();
	
	
	
	System.out.println("----------------------------\n");
	
	
	
		
    } catch (Exception e) {
	e.printStackTrace();
    }
  }
  
  public void outToConsole() {
		System.out.println("jnlpUrl : " + jnlpUrl);
		System.out.println("userName : " + userName);
		System.out.println("password : " + password);
		System.out.println("code : " + code);
		System.out.println("reportfile : " + reportfile);
		
		System.out.println("instrument : " + instrument);
		System.out.println("initialDeposit : " + initialDeposit);
		
		System.out.println("Leverage : " + leverage);
		System.out.println("MarginCutLevel : " + marginCutLevel);
		
		System.out.println("info : " + info);
		System.out.println("pathfile : " + pathfile);
		System.out.println("csvpathfile : " + csvpathfile);
		System.out.println("reportpathfile : " + reportpathfile);

		System.out.println("activeDB : " + activeDB);
		System.out.println("activeCSV : " + activeCSV);
		System.out.println("activeGateway : " + activeGateway);		
		
		System.out.println("GUI Mode : " + enableGUI);		
		System.out.println("activeFxOne: " + activeFxOne);	
		System.out.println("dateFrom : " + dateFrom);		
		System.out.println("dateTo: " + dateTo);	
  }
  
  public void outToConsole2() {
		System.out.println("intParam : " + intParam);
		System.out.println("doubleParam : " + doubleParam);
		System.out.println("boolParam : " + boolParam);
		System.out.println("textParam : " + textParam);
		System.out.println("file : " + file);
		System.out.println("currentTime : " + currentTime);
		System.out.println("color : " + color);
		System.out.println("instrument2 : " + instrument2);		
		System.out.println("period : " + period);		
  }  
  
  public void outToConsole3() {
	    System.out.println("name : " + name);
		System.out.println("amount : " + amount);	
		System.out.println("shift : " + shift);
		System.out.println("iperiod : " + iperiod);
		System.out.println("slPips : " + slPips);	
		System.out.println("tpPipsOnLoss : " + tpPipsOnLoss);	
		System.out.println("tpPipsOnProfit : " + tpPipsOnProfit);	
		System.out.println("setCalendar : " + setCalendar);	
		System.out.println("mode : " + mode);		
		System.out.println("enable : " + enable);
  }   

}
