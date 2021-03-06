/*
 * Copyright (c) 2009 Dukascopy (Suisse) SA. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * -Redistribution of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * -Redistribution in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 * 
 * Neither the name of Dukascopy (Suisse) SA or the names of contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING
 * ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE
 * OR NON-INFRINGEMENT, ARE HEREBY EXCLUDED. DUKASCOPY (SUISSE) SA ("DUKASCOPY")
 * AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE
 * AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES. IN NO EVENT WILL DUKASCOPY OR ITS LICENSORS BE LIABLE FOR ANY LOST
 * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL,
 * INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY
 * OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THIS SOFTWARE,
 * EVEN IF DUKASCOPY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
 */
package merFx;

import com.dukascopy.api.Instrument;
import com.dukascopy.api.LoadingProgressListener;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.ITesterClient.DataLoadingMethod;
import com.dukascopy.api.system.TesterFactory;

import fxOne.merFXFile;
import py4j.AdditionApplication;
import py4j.fxGateway;
import singlejartest.Main;
//import strategies.HolyTrinityStrategy;
//import strategies.OppositeOrder;
//import strategies.TURBO_trading_GBPUSD10min;
//import strategies.sto_rc2;
//import strategies.trailingStop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Future;
import java.text.SimpleDateFormat;

/**
 * This small program demonstrates how to initialize Dukascopy tester and start a strategy
 */
public class merTesterMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    // merFX
    private static int strategyCounter = 0;
    // end

    private static String jnlpUrl = "";
    private static String userName = "";
    private static String password = "";
    
    private static String instrumentText = ""; 
    
    private static double initialDeposit = 0;    
    private static int leverage = 0;    
    private static int marginCutLevel = 0;    
    private static String dateFrom = "";    
    private static String dateTo = ""; 
    
    //private static PythonCaller pycall = new PythonCaller();
    
    private static fxGateway gateway;
    
    //private static Optimizer optim = new Optimizer(32,6);

    
	public static void insertStringInFile(File inFile, int lineno, String lineToBeInserted) throws Exception {
		// temp file
		File outFile = new File("$$$$$$$$.tmp");

		// input
		FileInputStream fis = new FileInputStream(inFile);
		BufferedReader in = new BufferedReader(new InputStreamReader(fis));

		// output
		FileOutputStream fos = new FileOutputStream(outFile);
		PrintWriter out = new PrintWriter(fos);

		String thisLine = "";
		int i = 1;
		while ((thisLine = in.readLine()) != null) {
			if (i == lineno)
				out.println(lineToBeInserted);
			out.println(thisLine);
			i++;
		}
		out.flush();
		out.close();
		in.close();

		inFile.delete();
		outFile.renameTo(inFile);
	}    
    
    public static void main(String[] args) throws Exception {
    	
    	//read xml file
    	
    	final merFXFile fxXML = new merFXFile();
    	
    	jnlpUrl = fxXML.jnlpUrl; 
    	userName = fxXML.userName; 
    	password = fxXML.password;
    	instrumentText = fxXML.instrument; 
    	initialDeposit = Double.parseDouble(fxXML.initialDeposit);
    	leverage = Integer.parseInt(fxXML.leverage);
    	marginCutLevel = Integer.parseInt(fxXML.marginCutLevel);
    	dateFrom = fxXML.dateFrom; 
    	dateTo = fxXML.dateTo; 
    	
    	//fxXML.outToConsole();
 
        //get the instance of the IClient interface
        final ITesterClient client = TesterFactory.getDefaultInstance();
        
    	final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    	dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

    	//Date dateFrom = dateFormat.parse("01/03/2017 12:00:00");
    	//Date dateTo = dateFormat.parse("01/04/2017 00:00:00");
    	Date idateFrom = dateFormat.parse(dateFrom);
    	Date idateTo = dateFormat.parse(dateTo);    	
    	
    	client.setDataInterval(DataLoadingMethod.ALL_TICKS, idateFrom.getTime(), idateTo.getTime());        
        
        //fxGateway gateway = new fxGateway();
        
        //final StrategyParams2 strategy1;
        //set the listener that will receive system events
        client.setSystemListener(new ISystemListener() {
        	private int lightReconnects = 3;
        	
            @Override
            public void onStart(long processId) {
                LOGGER.info("Strategy started: " + processId);
            }

            @Override
            public void onStop(long processId) {
                LOGGER.info("Strategy stopped: " + processId);
                // merFX
                LOGGER.info("Strategy Counter: " + strategyCounter);
                // end
                
                
                java.util.Date date = new java.util.Date();
                //System.out.println(date);                
                // File reportFile = new File("D:\\workspaces\\neon\\IDE\\JForex-SDK\\logs\\report.html");  "yyMMddHHmmssZ"  "dd.MM.yyyy"
                File reportFile = new File(fxXML.reportpathfile +  fxXML.reportfile + new SimpleDateFormat("yyyyMMdd'_T'HHmmss").format(new java.util.Date()) + ".html");
                try {
                    client.createReport(processId, reportFile);
                    insertStringInFile(reportFile, 90, "<tr><th>__________INSERTED LINE__________</th><td>value</td></tr>");
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                if (client.getStartedStrategies().size() == 0) {
                    System.exit(0);
                }
            }

            @Override
            public void onConnect() {
                LOGGER.info("Connected");
                lightReconnects = 3;
            }

            @Override
            public void onDisconnect() {
            	//tester doesn't disconnect
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (lightReconnects > 0) {
                            client.reconnect();
                            --lightReconnects;
                        } else {
                            do {
                                try {
                                    Thread.sleep(60 * 1000);
                                } catch (InterruptedException e) {
                                }
                                try {
                                    if(client.isConnected()) {
                                        break;
                                    }
                                    client.connect(jnlpUrl, userName, password);
                                } catch (Exception e) {
                                    LOGGER.error(e.getMessage(), e);
                                }
                            } while(!client.isConnected());
                        }
                    }
                };
                new Thread(runnable).start();
            }
            
        });

        LOGGER.info("Connecting...");
        //connect to the server using jnlp, user name and password
        //connection is needed for data downloading
        client.connect(jnlpUrl, userName, password);

        //wait for it to connect
        int i = 10; //wait max ten seconds
        while (i > 0 && !client.isConnected()) {
            Thread.sleep(1000);
            i--;
        }
        if (!client.isConnected()) {
            LOGGER.error("Failed to connect Dukascopy servers");
            System.exit(1);
        }

        //set instruments that will be used in testing
        Set<Instrument> instruments = new HashSet<>();
        instruments.add(Instrument.EURUSD);
        LOGGER.info("Subscribing instruments...");
        client.setSubscribedInstruments(instruments);

        //setting initial deposit
        client.setInitialDeposit(Instrument.EURUSD.getSecondaryJFCurrency(), initialDeposit);        
        //Modifying account data
        client.setLeverage(leverage);
        client.setMarginCutLevel(marginCutLevel);
        
        //load data
        LOGGER.info("Downloading data");
        Future<?> future = client.downloadData(null);
        //wait for downloading to complete
        future.get();
                
        //call python
        //pycall;
        //start the strategy
        LOGGER.info("Starting strategy");
        
        merFX_Play strategy1 = new merFX_Play();
        merFX_Play strategy2 = new merFX_Play();
        
        strategy1.amount = 0.01;
        strategy1.sl_factor = 20;
        strategy1.tp_factor = 10;
        
        strategy2.amount = 0.02;
        strategy2.sl_factor = 60;
        strategy2.tp_factor = 60;        
        
        //final long strategyId1 = client.startStrategy(strategy1);

        
        final long strategyId2 = client.startStrategy(
        	//strategy2,
        	//new merFX_Play(),
        	new MA_Play(), 
			new LoadingProgressListener() {
        	@SuppressWarnings("serial")
        	private SimpleDateFormat sdf = new SimpleDateFormat(" MM/dd/yyyy HH:mm:ss,") {{
        	        setTimeZone(TimeZone.getTimeZone("GMT"));
        	}};
        	private boolean past18 = false;        		
        		
        		
            @Override
            public void dataLoaded(long startTime, long endTime, long currentTime, String information) {
            	// merFX
            	strategyCounter = strategyCounter +1;
                //LOGGER.info("merTesterFX: " + information);
                LOGGER.info("merTesterFX: " + information + sdf.format(startTime) + sdf.format(endTime) + sdf.format(currentTime ));
                //LOGGER.info("merTester: " + information + " Strategy Counter: " + strategyCounter);
                sdf.getCalendar().setTimeInMillis(currentTime);
                //check if we have loaded a chunk of data later than 18:00
                if (sdf.getCalendar().get(Calendar.HOUR_OF_DAY) >= 18){
                    LOGGER.info(" > 18");
                    past18 = true;
                } 
                // end
            }

            @Override
            public void loadingFinished(boolean allDataLoaded, long startTime, long endTime, long currentTime) {
            }
            
            //stop loading data if it is past 18:00
            @Override
            public boolean stopJob() {
                return false;
                //return past18;  //ERROR Main - Report data is not available
            }
        });
        //now it's running
        
        
        
      //every second check if "stop" had been typed in the console - if so - then stop the strategy
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {                
                Scanner s = new Scanner(System.in);                
                while(true){
                    while(s.hasNext()){
                        String str = s.next();
                        if(str.equalsIgnoreCase("stop")){
                            System.out.println("Strategy stop by console command.");
                            //client.stopStrategy(strategyId1);
                            client.stopStrategy(strategyId2);
                            break;
                        }
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            });
        thread.start();        
        
               
    }
}
