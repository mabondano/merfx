/*
 * Copyright (c) 2017 Dukascopy (Suisse) SA. All Rights Reserved.
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
package merFx.client.gui;

import com.dukascopy.api.IStrategy;
import com.dukascopy.api.Instrument;
import com.dukascopy.api.LoadingProgressListener;
import com.dukascopy.api.system.ISystemListener;
import com.dukascopy.api.system.ITesterClient;
import com.dukascopy.api.system.TesterFactory;
import com.dukascopy.api.system.tester.ITesterExecution;
import com.dukascopy.api.system.tester.ITesterUserInterface;

import fxOne.merFXFile;
import py4j.fxGateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.Future;

class merFXClientRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(merFXClientRunner.class);

    ITesterClient client;   
    //private static ITesterClient client;
    
    private static double initialDeposit = 0;    
    private static int leverage = 0;    
    private static int marginCutLevel = 0;    
    private static String dateFrom = "";    
    private static String dateTo = ""; 

    private static merFXFile fxXML = new merFXFile(); 
    @SuppressWarnings("unused")
	private static fxGateway gateway;
    
    @SuppressWarnings("unused")
	private static long strategyId;
    
    //private static PythonCaller pycall = new PythonCaller();    
    //private static fxGateway gateway = new fxGateway();    
    //private static Optimizer optim = new Optimizer(32,6);    

    void start(String jnlpUrl, String userName, String password, Instrument instrument, ITesterExecution testerExecution, ITesterUserInterface testerUserInterface, ISystemListener systemListener, IStrategy strategy) throws Exception {
        client = TesterFactory.getDefaultInstance();
        
        readXmlFile();

        client.setSystemListener(systemListener);
        tryToConnect(jnlpUrl, userName, password);
        setDataInterval(dateFrom, dateTo);
        subscribeInstrument(instrument);
        downloadDataAndWaitForResult();

        //start the strategy
        LOGGER.info("Starting strategy");

        strategyId = client.startStrategy(
                strategy,
                getLoadingProgressListener(),
                testerExecution,
                testerUserInterface
        );
        //now it's running
        checkStopByConsole(strategyId);
    }
    
    private static void readXmlFile() {   	
    	initialDeposit = Double.parseDouble(fxXML.initialDeposit);
    	leverage = Integer.parseInt(fxXML.leverage);
    	marginCutLevel = Integer.parseInt(fxXML.marginCutLevel);
    	dateFrom = fxXML.dateFrom; 
    	dateTo = fxXML.dateTo;   	
    }    

    private LoadingProgressListener getLoadingProgressListener() {
        return new LoadingProgressListener() {
            @Override
            public void dataLoaded(long startTime, long endTime, long currentTime, String information) {
                LOGGER.info(information);
            }

            @Override
            public void loadingFinished(boolean allDataLoaded, long startTime, long endTime, long currentTime) {
            }

            @Override
            public boolean stopJob() {
                return false;
            }
        };
    }

    private void downloadDataAndWaitForResult() throws InterruptedException, java.util.concurrent.ExecutionException {
        client.setInitialDeposit(Instrument.EURUSD.getSecondaryJFCurrency(), initialDeposit);
        client.setLeverage(leverage);
        client.setMarginCutLevel(marginCutLevel);
        LOGGER.info("Downloading data");
        Future<?> future = client.downloadData(null);
        future.get();
    }

    private void tryToConnect(String jnlpUrl, String userName, String password) throws Exception {
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
    }


    private void subscribeInstrument(Instrument instrument) {
        final Set<Instrument> instruments = new HashSet<>();
        instruments.add(instrument);

        LOGGER.info("Subscribing instruments...");
        client.setSubscribedInstruments(instruments);
    }

    private void setDataInterval(String dateFrom, String dateTo) throws ParseException {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));

        Date dateFromObject = dateFormat.parse(dateFrom);
        Date dateToObject = dateFormat.parse(dateTo);

        client.setDataInterval(ITesterClient.DataLoadingMethod.ALL_TICKS, dateFromObject.getTime(), dateToObject.getTime());
        LOGGER.info("from: " + dateFrom.toString() + " to: " + dateTo.toString());
    }
    
    private void checkStopByConsole(final long strategyId) {
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
                            client.stopStrategy(strategyId);
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
