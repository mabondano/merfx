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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.dukascopy.api.*;

public class MA_Play implements IStrategy {
	
    private IEngine engine = null;
    private IIndicators indicators = null;
    private int tagCounter = 0;
    private double[] ma1 = new double[Instrument.values().length];
    
    private IConsole console;
    private IHistory history;
    
    private SimpleDateFormat gmtSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      
	private Period period = Period.TEN_SECS;
	private Instrument instrument = Instrument.EURUSD;
	


	@Override
	public void onStart(IContext context) throws JFException {
        engine = context.getEngine();
        indicators = context.getIndicators();
        this.console = context.getConsole();
        console.getOut().println("Started");
        
        history = context.getHistory();
        gmtSdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        
        //putting out all data to console
        try {
           //Iterating thru parsed array
            for(Double d : parse("D:/workspaces/neon/Dev/csv/EURUSD_history.csv")){
                this.console.getOut().println(d);
            }
        } catch (Exception e) {            
          this.console.getErr().println(e.getMessage());
        }
    }
    
	//use of string operations
	private boolean isValidTime(int fromHour, int fromMin, int toHour, int toMin) throws JFException {			

		boolean result = false;
		long lastTickTime = history.getLastTick(instrument).getTime();
		//you want to work with the date of the last tick - in a case you are back-testing
		String fromStr = gmtSdf.format(lastTickTime).substring(0, 11) + String.valueOf(fromHour)+":"+String.valueOf(fromMin) + ":00";
		String toStr = gmtSdf.format(lastTickTime).substring(0, 11) + String.valueOf(toHour)+":"+String.valueOf(toMin) + ":00";
		try {
			long from = gmtSdf.parse(fromStr).getTime();
			long to = gmtSdf.parse(toStr).getTime();
			
			print(String.format("calendar: %s - %s last tick: %s", gmtSdf.format(from), gmtSdf.format(to), gmtSdf.format(lastTickTime)));
			result = lastTickTime > from  && lastTickTime < to;			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	@Override
    public void onStop() throws JFException {
        for (IOrder order : engine.getOrders()) {
            order.close();
        }
        console.getOut().println("Stopped");
    }
    @Override
    public void onTick(Instrument instrument, ITick tick) throws JFException {
    	//Filter Ticks 
    	//if (!instrument.equals(Instrument.EURUSD)) return; //filter EUR/USD ticks
    	
        if (ma1[instrument.ordinal()] == -1) {
            ma1[instrument.ordinal()] = indicators.ema(instrument, Period.TEN_SECS, OfferSide.BID, IIndicators.AppliedPrice.MEDIAN_PRICE, 14, 1);
        }
        double ma0 = indicators.ema(instrument, Period.TEN_SECS, OfferSide.BID, IIndicators.AppliedPrice.MEDIAN_PRICE, 14, 0);
        if (ma0 == 0 || ma1[instrument.ordinal()] == 0) {
            ma1[instrument.ordinal()] = ma0;
            return;
        }

        double diff = (ma1[instrument.ordinal()] - ma0) / (instrument.getPipValue());

        if (positionsTotal(instrument) == 0) {
            if (diff > 1) {
                engine.submitOrder(getLabel(instrument), instrument, IEngine.OrderCommand.SELL, 0.001, 0, 0, tick.getAsk()
                        + instrument.getPipValue() * 10, tick.getAsk() - instrument.getPipValue() * 15);
            }
            if (diff < -1) {
                engine.submitOrder(getLabel(instrument), instrument, IEngine.OrderCommand.BUY, 0.001, 0, 0, tick.getBid()
                        - instrument.getPipValue() * 10, tick.getBid() + instrument.getPipValue() * 15);
            }
        }
        ma1[instrument.ordinal()] = ma0;
    }
    @Override
    public void onBar(Instrument instrument, Period period, IBar askBar, IBar bidBar) throws JFException {
    	//Filter Bars
    	if (!instrument.equals(Instrument.EURUSD) || !period.equals(Period.ONE_HOUR)) return; //filter ONE_HOUR EUR/USD bars
		//if (period != this.period || instrument != this.instrument)
		//	return;

		print ( "Is valid time? " + isValidTime (10, 0, 18, 0) );
    }

	private void print(Object o) {
		console.getOut().println(o);
	}
    //count open positions
    protected int positionsTotal(Instrument instrument) throws JFException {
        int counter = 0;
        for (IOrder order : engine.getOrders(instrument)) {
            if (order.getState() == IOrder.State.FILLED) {
                counter++;
            }
        }
        return counter;
    }

    protected String getLabel(Instrument instrument) {
        String label = instrument.name();
        label = label.substring(0, 2) + label.substring(3, 5);
        label = label + (tagCounter++);
        label = label.toLowerCase();
        return label;
    }
    @Override
    public void onMessage(IMessage message) throws JFException {
    }
    @Override
    public void onAccount(IAccount account) throws JFException {
    }
    
    
    /**
     * Takes StringTokenizer as input and returns second token
     * If token dosen't exists return String "0"
     * 
     * @param StringTokenizer st
     * @return String token
     */
    private static String secondToken(StringTokenizer st){
        try{
            st.nextToken();
            return st.nextToken();
        }catch (NoSuchElementException nsee){
           //if in token String is no token symbols 
           //we just return 0   
            return "0";
        }        
    }

    /**
     * Parse file by given path in directory tree. Takes from file each strokes 
     * second token and place it as Double into ArrayList 
     * 
     * @param path - file path
     * @return ArrayList of Doubles  
     * @throws IOException
     */
    private static List<Double> parse(String filePath) throws IOException{

        String path = filePath;  

        List<Double> prices = new ArrayList<Double>();
        File file = new File(path);

        BufferedReader bufRdr  = new BufferedReader(new FileReader(file));
        String line = null;

        //read each line of text file        
        while((line = bufRdr.readLine()) != null)
        {
           StringTokenizer st = new StringTokenizer(line,",");

            //get token and store it as Double in the array
            prices.add(Double.valueOf(secondToken(st)));

        }
        //close the file
        bufRdr.close();

        return prices;
    }
}