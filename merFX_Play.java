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
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.TimeZone;

import com.dukascopy.api.*;

import fxOne.merFXFile;

public class merFX_Play implements IStrategy {
	
	final merFXFile ifxXML = new merFXFile();
	
	private String mPeriod = ifxXML.period;  
	private String mInstrument = ifxXML.instrument; 
	private String mSetCalendar = ifxXML.setCalendar;
	
	private int iPeriod = Integer.parseInt(ifxXML.iperiod); 
	private int ishift = Integer.parseInt(ifxXML.shift); 
	private double amount = Double.parseDouble(ifxXML.amount); 
	private int sl_factor = Integer.parseInt(ifxXML.tpPipsOnLoss);
	private int tp_factor = Integer.parseInt(ifxXML.tpPipsOnProfit);
	boolean ienable = Boolean.parseBoolean(ifxXML.enable); 

	//int y = Integer.parseInt(str);
	//Integer x = Integer.valueOf(str); 
	//String text = "12.34"; // example String
	//double value = Double.parseDouble(text);
	//Boolean boolean1 = Boolean.valueOf("true");
	//boolean boolean2 = Boolean.parseBoolean("true");
	/*
	 * *******************************************************************************
	 */	
	
    private IEngine engine = null; 
    private IIndicators indicators = null;
    private int tagCounter = 0;
    private double[] ma1 = new double[Instrument.values().length];
    
    private IConsole console;
    private IHistory history;
    
    private SimpleDateFormat gmtSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      
	private Period period = setPeriod(mPeriod);
	private Instrument instrument = setInstrument(mInstrument);
	
	
    protected Period setPeriod(String value) {
 
       if (value == "TICK"){ period = Period.TICK;
       
       } else if (value == "ONE_HOUR"){ period = Period.ONE_HOUR;
       } else if (value == "ONE_MIN"){ period = Period.ONE_MIN;
       } else if (value == "ONE_SEC"){ period = Period.ONE_SEC;
       
       } else if (value == "TWO_SECS"){ period = Period.TWO_SECS;
 
       } else if (value == "FIVE_MINS"){ period = Period.FIVE_MINS;
       
       } else if (value == "TEN_MINS"){ period = Period.TEN_MINS;  
       } else if (value == "TEN_SECS"){ period = Period.TEN_SECS; 
       
       } else if (value == "FIFTEEN_MINS"){ period = Period.FIFTEEN_MINS;
       
       } else if (value == "TWENTY_MINS"){ period = Period.TWENTY_MINS;
       } else if (value == "TWENTY_SECS"){ period = Period.TWENTY_SECS;
             
       } else if (value == "THIRTY_MINS"){ period = Period.THIRTY_MINS;
       } else if (value == "THIRTY_SECS"){ period = Period.THIRTY_SECS;         
       
       } else if (value == "FOUR_HOURS"){ period = Period.FOUR_HOURS;
       } else if (value == "DAILY"){ period = Period.DAILY;
       } else if (value == "WEEKLY"){ period = Period.WEEKLY;
       } else if (value == "MONTHLY"){ period = Period.MONTHLY;
       } else if (value == "INFINITY"){ period = Period.INFINITY;   
       
       } else period = Period.TEN_SECS; 

  	
    	return period;
    }	
	
    protected Instrument setInstrument(String value) {
    	 
        if (value == "EURUSD"){ instrument = Instrument.EURUSD;
        
        } else if (value == "EURJPY"){ instrument = Instrument.EURJPY;
        } else if (value == "USDJPY"){ instrument = Instrument.USDJPY;
        } else if (value == "USDRUB"){ instrument = Instrument.USDRUB;
        } else if (value == "AUDUSD"){ instrument = Instrument.AUDUSD;   
 
        } else instrument = Instrument.EURUSD; 
 	
     	return instrument;  
     }	
 		
	
	
	
	
	
	
	
	
	
	/*
	 * *******************************************************************************
	 * */

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
    	
    	if (ienable) {
    			
	        if (ma1[instrument.ordinal()] == -1) {
	            ma1[instrument.ordinal()] = indicators.ema(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.MEDIAN_PRICE, iPeriod, ishift);
	        }
	        double ma0 = indicators.ema(instrument, period, OfferSide.BID, IIndicators.AppliedPrice.MEDIAN_PRICE, iPeriod, 0);
	        if (ma0 == 0 || ma1[instrument.ordinal()] == 0) {
	            ma1[instrument.ordinal()] = ma0;
	            return;
	        }
	
	        double diff = (ma1[instrument.ordinal()] - ma0) / (instrument.getPipValue());
	
	        if (positionsTotal(instrument) == 0) {
	            if (diff > 1) {
	                engine.submitOrder(getLabel(instrument), instrument, IEngine.OrderCommand.SELL, amount, 0, 0, tick.getAsk()
	                        + instrument.getPipValue() * sl_factor, tick.getAsk() - instrument.getPipValue() * tp_factor);
	            }
	            if (diff < -1) {
	                engine.submitOrder(getLabel(instrument), instrument, IEngine.OrderCommand.BUY, amount, 0, 0, tick.getBid()
	                        - instrument.getPipValue() * sl_factor, tick.getBid() + instrument.getPipValue() * tp_factor);
	            }
	        }
	        ma1[instrument.ordinal()] = ma0;
        
    	}
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


/*
	FIFTEEN_MINS 
	FIVE_MINS 
	FOUR_HOURS 
	INFINITY 
	MONTHLY 
	ONE_HOUR 
	ONE_MIN 
	ONE_SEC   		
	TEN_MINS 
	TEN_SECS        
	THIRTY_MINS 
	THIRTY_SECS 
	TICK 
	TWENTY_MINS 
	TWENTY_SECS
	TWO_SECS
	WEEKLY
*/  