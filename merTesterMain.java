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
import com.dukascopy.api.system.TesterFactory;

import fxOne.merFXFile;
import py4j.AdditionApplication;
import py4j.fxGateway;
import singlejartest.Main;
import strategies.HolyTrinityStrategy;
import strategies.OppositeOrder;
import strategies.TURBO_trading_GBPUSD10min;
import strategies.sto_rc2;
import strategies.trailingStop;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
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

    //url of the DEMO jnlp
    private static String jnlpUrl = "";
    //user name
    private static String userName = "";
    //password
    private static String password = "";
    
    private static String instrumentText = ""; 
    
    private static double initialDeposit = 0; 
    
    //private static PythonCaller pycall = new PythonCaller();
    
    private static fxGateway gateway;
    
    //private static Optimizer optim = new Optimizer(32,6);

    public static void main(String[] args) throws Exception {
    	
    	//read xml file
    	
    	final merFXFile fxXML = new merFXFile();
    	
    	jnlpUrl = fxXML.jnlpUrl; 
    	userName = fxXML.userName; 
    	password = fxXML.password;
    	instrumentText = fxXML.instrument; 
    	initialDeposit = Double.parseDouble(fxXML.initialDeposit);
    	
    	//fxXML.outToConsole();
 
        //get the instance of the IClient interface
        final ITesterClient client = TesterFactory.getDefaultInstance();
        
        //fxGateway gateway = new fxGateway();
        
        //final StrategyParams2 strategy1;
        //set the listener that will receive system events
        client.setSystemListener(new ISystemListener() {
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
            }

            @Override
            public void onDisconnect() {
                //tester doesn't disconnect
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
        //load data
        LOGGER.info("Downloading data");
        Future<?> future = client.downloadData(null);
        //wait for downloading to complete
        future.get();
                
        //call python
        //pycall;
        //start the strategy
        LOGGER.info("Starting strategy");
        client.startStrategy(
        	new merFX_Play(),
        	//new MA_Play(), 
			//new T3TrendAlerter(),
			//new CheckMarketHoursSdf(), 
        		//new CheckMarketHoursCalendar(), 
            //new CheckMarketHoursCalendarDay(), 
			//new GetCSVDataStartegy(),
        	//new FullMarketDepth(),
        	//new SmaFlipMinMax(),
        	//new LoadClassExample(),
        	//new InstrumentCounters(),   	
        	//new OrderUpdateTableStrategy2(),
        	//new SimpleTpSlStrategy(),
        	//new StrategyParams(),
        	//new ConfigOptionsEnum(),
        	//new MinAmounts(),
        		//TestMail
        		//TestMySQLAccess
        		//MarketOfflineHours
        		//MarketIsOffline
        		//GetBarsExample
        		//new HistoryBarsSynch(),
        		//new SmaTrendStrategy(),
        		//new SmaCrossStrategy(),
        		//new SmaCrossStrategyVisual(),
        		//new SMAStrategy(),
        		//new SMASimpleStrategy(),
        		//new MartingaleWtihMA(),
        	//new DailyMartinGale(),
        	//new Stochastic(),
        		//new OppositeOrder(),
        		//new TURBO_trading_GBPUSD10min(),
			new LoadingProgressListener() {
            @Override
            public void dataLoaded(long startTime, long endTime, long currentTime, String information) {
            	// merFX
            	strategyCounter = strategyCounter +1;
                LOGGER.info("merTesterFX: " + information);
                //LOGGER.info("merTester: " + information + " Strategy Counter: " + strategyCounter);
                // end
            }

            @Override
            public void loadingFinished(boolean allDataLoaded, long startTime, long endTime, long currentTime) {
            }

            @Override
            public boolean stopJob() {
                return false;
            }
        });
        //now it's running
    }
}
