import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Arrays;

import java.time.LocalDateTime;
import java.sql.Timestamp;

public class GigSystem {

    public static void main(String[] args) {

        // You should only need to fetch the connection details once.
        // You might need to change this to either getSocketConnection() or getPortConnection() - see below
        Connection conn = getSocketConnection();

        boolean repeatMenu = true;

        while(repeatMenu){
            System.out.println("_________________________________________");
            System.out.println("________________GigSystem________________");
            System.out.println("_________________________________________");

            System.out.println("___________1: Get Gig Line-up____________");
            System.out.println("______________2: Add a Gig_______________");
            System.out.println("__________3: Purchase a Ticket___________");
            System.out.println("_____________4: Cancel act_______________");
            System.out.println("__5: Tickets to Sell for Gig Vialbility__");
            System.out.println("__________6: Check Ticket Sales__________");
            System.out.println("________7: Get Regular Customers_________");
            System.out.println("______8: Economically Feasible Gigs______");

            
            System.out.println("q: Quit");

            String menuChoice = readEntry("Please choose an option: ");

            if(menuChoice.length() == 0){
                //Nothing was typed (user just pressed enter) so start the loop again
                continue;
            }
            char option = menuChoice.charAt(0);

            /**
             * If you are going to implement a menu, you must read input before you call the actual methods
             * Do not read input from any of the actual task methods
             */
            switch(option){
                case '1':
                    int ID;
                    String gigID = readEntry("What is the ID of the gig you wish to see the lineup for? : "); //presents the query to the user
                    try{
                        ID = Integer.parseInt(gigID); //turns the gigID string into an integer reprsentation
                    }catch(Exception e){
                        System.err.println("Invalid ID input");
                        break; //breaks out of the switch case
                    }

                    String[][] tableArray = null;
                    try{
                        tableArray = task1(conn, ID);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    printTable(tableArray);
                    
                    break;


                case '2':
                    //gets the various input required for the gig information
                    String venueName = readEntry("What is the name of the venue where this gig takes place? : ");
                    String gigTitle = readEntry("What is the title for this gig? : ");
                    String gigTimeInput = readEntry("What is the date and time for this gig? (in the form: YYYY:MM:DD:HH:MM:SS) : ");
                    String adultTicketPriceStr = readEntry("What is the price for an adult ticket? : ");

                    Boolean actLoop = true;

                    System.out.println("Enter details for each act below, one by one: \n");
                    ArrayList<String> actArrayList = new ArrayList<String>(); //defines an array list to hold all of the act information
                    while(actLoop == true){ //iterates as long as the user wants to enter another act
                        //gets all the information for each act
                        String actIDStr = readEntry("What is the actID? : ");
                        String actFeeStr = readEntry("What is the fee of the act? : ");
                        String actStartStr = readEntry("What is the date and time for this act? (in the from YYYY:MM:DD:HH:MM:SS) : ");
                        String actDurationStr = readEntry("How long will this act last for? (in minutes) : ");
                        
                        //appends all the act information together
                        String actInfo = (actIDStr+"@"+actFeeStr+"@"+actStartStr+"@"+actDurationStr);
                        actArrayList.add(actInfo); //adds the act to the array
                        
                        //asks the user if they'd like to loop and enter another act
                        String repeat = readEntry("Would you like to enter another act? (y/n) : ");
                        if (repeat.equalsIgnoreCase("n")){
                            actLoop = false;
                        }
                    }
                    

                    LocalDateTime gigStart = null;
                    int adultTicketPrice = -1;
                    ActPerformanceDetails[] actDetails = null;
                    
                    //turns all inputs currently not in the correct form, into their correct form
                    try{

                        //creates the ActPerformanceDetails array
                        ArrayList<ActPerformanceDetails> ActPerformanceDetailsArrayLst = new ArrayList<ActPerformanceDetails>(); //creates an array list to store the act performance details
                        for(int i=0; i<actArrayList.size(); i++){ //loops over the number of acts to enter
                            String[] actParts = actArrayList.get(i).split("@"); //splits the act array into its composite parts

                            //parses the int inputs into integers
                            int actID = Integer.parseInt(actParts[0]);
                            int actFee = Integer.parseInt(actParts[1]);
                            int actDuration = Integer.parseInt(actParts[3]);

                            //splits the input into parts so it can be transformed into a LocalDateTime
                            String[] actTimeParts = actParts[2].split(":"); //splits the time entry by ":"
                            int year = Integer.parseInt(actTimeParts[0]);
                            int month = Integer.parseInt(actTimeParts[1]);
                            int day = Integer.parseInt(actTimeParts[2]);
                            int hour = Integer.parseInt(actTimeParts[3]);
                            int minute = Integer.parseInt(actTimeParts[4]);
                            int second = Integer.parseInt(actTimeParts[5]);
                            LocalDateTime actStart = LocalDateTime.of(year, month, day, hour, minute, second); //transforms into a local date time

                            ActPerformanceDetailsArrayLst.add(new ActPerformanceDetails(actID, actFee, actStart, actDuration)); //adds the the act to the ActPerformanceDetailsArrayLst as an ActPerformanceDetails Object
                        }
                        //turn the array list into an array
                        actDetails = new ActPerformanceDetails[ActPerformanceDetailsArrayLst.size()]; //creates an array of the size of all of the acts entered
                        for(int i=0; i<actDetails.length; i++){ //iterates over the length of the array
                            actDetails[i] =ActPerformanceDetailsArrayLst.get(i); //defines each index
                        }



                        //splits the time input into parts so it can be transformed into a LocalDateTime for the gig start time
                        String[] timeParts = gigTimeInput.split(":");
                        int year = Integer.parseInt(timeParts[0]);
                        int month = Integer.parseInt(timeParts[1]);
                        int day = Integer.parseInt(timeParts[2]);
                        int hour = Integer.parseInt(timeParts[3]);
                        int minute = Integer.parseInt(timeParts[4]);
                        int second = Integer.parseInt(timeParts[5]);
                        gigStart = LocalDateTime.of(year, month, day, hour, minute, second); //transforms the inputs to a LocalDateTime type


                        adultTicketPrice = Integer.parseInt(adultTicketPriceStr); //turns the ticket price input into an integer

                    }catch(Exception e){
                        e.printStackTrace();
                        System.err.println("Your inputs are not of a valid form");
                        break;
                    }

                    task2(conn, venueName, gigTitle, gigStart, adultTicketPrice, actDetails); //calls task 2 with the necessary information
                    
                    break;

                case '3':
                    System.out.println("Please enter the details for the ticket below: ");
                    String name = readEntry("Customer name: ");
                    String email = readEntry("Customer email: ");
                    String ticketType = readEntry("What is the type of the ticket? : ");
                    String gigid = readEntry("Gig ID: ");
                    int idnum = Integer.parseInt(gigid);

                    task3(conn, idnum, name, email, ticketType);
                    break;

                case '4':
                    String actName = readEntry("What is the name of the act that has cancelled? : ");
                    String cancelledGigID = readEntry("What is the ID of the gig that the act has cancelled for? : ");
                    int cancelledGigNum = Integer.parseInt(cancelledGigID);

                    task4(conn, cancelledGigNum, actName);

                    break;
                case '5':
                    task5(conn);
                    break;
                case '6':
                    task6(conn);
                    break;
                case '7':
                    task7(conn);
                    break;
                case '8':
                    task8(conn);
                    break;
                case 'q':
                    repeatMenu = false;
                    break;
                default: 
                    System.out.println("Invalid option");
            }
        }
    }











    /*
     * You should not change the names, input parameters or return types of any of the predefined methods in GigSystem.java
     * You may add extra methods if you wish (and you may overload the existing methods - as long as the original version is implemented)
     */

    /**
     * 
     * @param conn -the connection to database
     * @param gigID - the id of the gig we want to view
     * @return - the string[][] which contains the information about the gig lineup
     */
    public static String[][] task1(Connection conn, int gigID){ //Gig Line-Up
        try{
            String lineupQuery = "SELECT actname, TO_CHAR(actstart, 'HH24:MI') AS actstarts, TO_CHAR(actend, 'HH24:MI') AS actends FROM gigLineup WHERE gigID = ? ORDER BY viewOrderingNum"; //the query for getting the information about the lineup
            PreparedStatement getGigLineup = conn.prepareStatement(lineupQuery); //projects the necessary information limited to the specified format
            getGigLineup.setInt(1,gigID); //sets the question mark in the query to the gigID provided
            ResultSet gigLineupResults = getGigLineup.executeQuery(); //executes the getGigLineup query and stores it as the resultset gigLineupResults
            
            String[][] resultArray = convertResultToStrings(gigLineupResults); //turns the resultset into a 2D array
            
            //closes ResultSet and Connection
            gigLineupResults.close();
            getGigLineup.close();

            printTable(resultArray);

            return resultArray; //returns the final array 

        } catch(Exception e){ //catches any errors that occur
            e.printStackTrace(); 
            String[][] resultArray = new String[1][3]; //sets as an empty array
            return resultArray; //returns the empty array
        } 
    }














    /**
     * 
     * @param conn - the connetion to the database
     * @param venue - the name of the venue where this gig will be performed (String)
     * @param gigTitle - the name for the gig (String)
     * @param gigStart - the time that the gig will start (LocalDateTime)
     * @param adultTicketPrice - the cost of an adult ticket to the gig (Int)
     * @param actDetails - an array of objects containing the information about the acts (ActPerformanceDetails [])
     * 
     * This method inputs information into the gig, act_gig and gig_ticket tables.
     * It adds all information to these tables required to define a new gig on the schedule.
     */
    public static void task2(Connection conn, String venue, String gigTitle, LocalDateTime gigStart, int adultTicketPrice, ActPerformanceDetails[] actDetails){
        //actPerformance [][actID, fee, ontime, duration]
        //adultTicketPrice - standard adult ticket cost
        int venueID;
        int gigID;

        Savepoint savepoint = null;
        try{
            conn.setAutoCommit(false); //sets autocommit to false to ensure databse integrity
            savepoint = conn.setSavepoint(); //saves the current state of the database
        } catch(Exception e0){ //if there is an error with either of these actions
            e0.printStackTrace();
            return; //exits out of the method since database security cannot be assured
        }
        
        try{
            conn.setAutoCommit(false); //sets autocommit to false to ensure database integrity

            
            //gets the venueID linked to the venue given
            String selectVenue = "SELECT venueID FROM venue WHERE venuename = ?";
            PreparedStatement getVenueID = conn.prepareStatement(selectVenue);
            getVenueID.setString(1,venue);
            ResultSet venueIDSet = getVenueID.executeQuery(); //gets the result of executing the query
            venueIDSet.next(); //moves to the first row of the resultset
            venueID = venueIDSet.getInt(1); //gets the venueID out of the resultset
            venueIDSet.close();
            getVenueID.close();
            



            //inputs the gig into the database
            String createGigQuery = "INSERT INTO gig (gigID, venueID, gigtitle, gigdatetime, gigstatus) VALUES (DEFAULT,?,?,?,'G')";
            PreparedStatement insertGig = conn.prepareStatement(createGigQuery);
            insertGig.setInt(1, venueID);
            insertGig.setString(2, gigTitle);
            insertGig.setTimestamp(3, Timestamp.valueOf(gigStart)); //casts the gigdatetime into a timestamp for SQL first
            insertGig.executeUpdate(); //executes the gig input
            insertGig.close();




            //find the gigID of the gig we've just inputted
            //although we don't know the gigID, the gig venue and the specific time should indicate uniqueness, otherwise an error would've occurred when trying to input the gig (there'd be a clash)
            //there may be a gig at the same venue and time which has been cancelled, this doesn't cause an error, so the query selects only those gigs which are 'G' for "going ahead"
            String getGigIDQuery = "SELECT gigID FROM gig WHERE venueID = ? AND gigdatetime = ? AND gigstatus = 'G'";
            PreparedStatement getGigID = conn.prepareStatement(getGigIDQuery); //prepares the query
            //defines the parameters of the query
            getGigID.setInt(1,venueID);
            getGigID.setTimestamp(2, Timestamp.valueOf(gigStart));
            ResultSet gigIDSet = getGigID.executeQuery(); //stores the result for the gigID query as result set
            gigIDSet.next(); //moves to the first row of the resultset
            gigID = gigIDSet.getInt(1); //gets the gigID out of the resultset
            getGigID.close();
            gigIDSet.close();


            //inputting the acts into the act_gig table
            String inputActGigQuery = "INSERT INTO act_gig (actID, gigID, actgigfee, ontime, duration) VALUES "; //defines the start of the insert query

            for (int i=0; i<actDetails.length; i++){ //does once for each act in actDetails
                inputActGigQuery+="(?,?,?,?,?)"; //adds question marks to the query based on the amount of act objects
                if (i != actDetails.length - 1){ //if this isn't the final act to be inputted
                    inputActGigQuery+=","; //adds a comma between each set of question marks
                }
            }

            PreparedStatement inputActGig = conn.prepareStatement(inputActGigQuery); //defines the prepared statement

            //inputs the information for each act in actDetails
            for (int i=0; i<actDetails.length; i++){
                inputActGig.setInt(1+(i*5), actDetails[i].getActID()); //sets the actID 
                inputActGig.setInt(2+(i*5), gigID); //sets the gigID
                inputActGig.setInt(3+(i*5), actDetails[i].getFee()); //sets the act fee
                inputActGig.setTimestamp(4+(i*5), Timestamp.valueOf(actDetails[i].getOnTime())); //sets the act ontime
                inputActGig.setInt(5+(i*5), actDetails[i].getDuration()); //sets the act duration
            }
            inputActGig.executeUpdate(); //inputs all of the act_gig values
            inputActGig.close(); //closes the prepared statement


            //inputs the information into the gig_ticket table for this gig
            String inputGigTicketQuery = "INSERT INTO gig_ticket (gigID, pricetype, price) VALUES (?,?,?)";
            PreparedStatement inputGigTicket = conn.prepareStatement(inputGigTicketQuery);
            inputGigTicket.setInt(1, gigID);
            inputGigTicket.setString(2, "A");
            inputGigTicket.setInt(3, adultTicketPrice);
            inputGigTicket.executeUpdate();
            inputGigTicket.close();


            gigEnd(conn, gigID, gigStart); //runs the method which will check that the gig finishes early enough
            gigLength(conn, gigID); //runs the method which will check that the gig is at least 60 minutes long after the inserts
            gigStartMethod(conn, gigID); //runs the method which will check that the first act tarts at the same time as the gig starts
            actTravel(conn, gigID); //runs the method which will check that each act has enough time to travel to wherever they may need to be next
            actInterval(conn, gigID);//runs the method which will check that the intervals between acts is valid
            venueTurnoverCheck(conn, gigID); //runs the method which will check that the turnover time betweeen gigs is valid
            venueGigOverlapCheck(conn); //runs method which will check that there is no overlap between gigs at the same venue
            gigOverlapCheck(conn); //runs method which will check that acts are only scheduled to be in one place at a time
            actOverlapCheck(conn); //runs method which will check that there is no overlap between acts at the same gig

            conn.commit(); //commits the changes to each table into the database

        }catch(Exception e1){
            e1.printStackTrace();

            try{
                conn.rollback(savepoint); //if there is an error, return to the savepoint before any database interactions (ensures that the database is intact)
                
            }catch(Exception e2){
                e2.printStackTrace();
            }
        }
    }












    public static void task3(Connection conn, int gigid, String name, String email, String ticketType){
        Savepoint savepoint = null;
        try{
            conn.setAutoCommit(false); //sets autocommit to false to ensure databse integrity
            savepoint = conn.setSavepoint(); //saves the current state of the database
        } catch(Exception e0){ //if there is an error with either of these actions
            e0.printStackTrace();
            return; //exits out of the method since database security cannot be assured
        }


        try{
            conn.setAutoCommit(false); //sets the auto commit to false to ensure database security

            //checks that the gig exists and that it is going ahead
            String checkGigStatusQuery = "SELECT COUNT(*) FROM gig WHERE gigID = ? AND gigstatus = 'G'";
            PreparedStatement checkGigStatus = conn.prepareStatement(checkGigStatusQuery);
            checkGigStatus.setInt(1, gigid);

            ResultSet gigStatusRS = checkGigStatus.executeQuery(); //gets a result set from the query
            gigStatusRS.next(); //moves the resultset pointer
            int checkNum = gigStatusRS.getInt(1); //gets the integer resulting from the query

            //closes the resultset and preparedstatement
            gigStatusRS.close();
            checkGigStatus.close();

            if(checkNum!=1){ //checks whether there was an entry in the database which matches the query
                throw new Exception("There is no scheduled gig with the ID " + gigid); //throws an exception if there was no such gig
            }
            


            //gets the price of the ticket from the gig_ticket table relating to the ticketType and gigid provided
            String getGigTicketQuery = "SELECT price FROM gig_ticket WHERE gigID = ? AND pricetype = ?";
            PreparedStatement getGigTicket = conn.prepareStatement(getGigTicketQuery);
            getGigTicket.setInt(1, gigid);
            getGigTicket.setString(2, ticketType);

            ResultSet gigTicketPrice = getGigTicket.executeQuery(); //gets the result of the query
            if(! gigTicketPrice.next()){ //moves the pointer to the next part of the resultset, checking that this can be done in an if
                //closes the preparedstatement and resultset 
                gigTicketPrice.close();
                getGigTicket.close();
                throw new Exception("There are no tickets which match price type "+ticketType+" and gig "+gigid); //throws an exception about the issue
            } 
            
            int ticketCost = gigTicketPrice.getInt(1); //gets the cost of the ticket
            
            //closes the preparedstatement and resultset
            gigTicketPrice.close();
            getGigTicket.close();




            String checkTicketAmountQuery = 
                "WITH " +
                    "venueCapacity AS ( " + //gets the capacity of the venue where the gig is being held
                        "SELECT capacity " +
                        "FROM venue " +
                        "WHERE venueID = (SELECT venueID FROM gig WHERE gigID = ?) " +
                    ") " +

                "SELECT " + //checks whether the number of tickets for the gig is equal to the capacity
                    "CASE " +
                        "WHEN (SELECT COUNT(*) FROM ticket WHERE gigID = ?) >= (SELECT capacity FROM venueCapacity) THEN 1 " + //returns 1 if it is equal
                        "ELSE 0 " + //returns 0 otherwise
                    "END";
            PreparedStatement checkTicketAmount = conn.prepareStatement(checkTicketAmountQuery);
            checkTicketAmount.setInt(1, gigid);
            checkTicketAmount.setInt(2, gigid);

            ResultSet ticketMaxRS = checkTicketAmount.executeQuery();
            ticketMaxRS.next();
            int ticketMaxNum = ticketMaxRS.getInt(1);

            ticketMaxRS.close();
            checkTicketAmount.close();

            if(ticketMaxNum==1){
                throw new Exception("Sorry, the gig is sold out!");
            }
            
                
 


            //inserts into the ticket table a new ticket record with all the correct information
            String ticketInsertQuery = "INSERT INTO ticket (ticketID, gigID, pricetype, cost, customername, customeremail) VALUES (DEFAULT,?,?,?,?,?)";
            PreparedStatement ticketInsert = conn.prepareStatement(ticketInsertQuery);
            ticketInsert.setInt(1, gigid);
            ticketInsert.setString(2, ticketType);
            ticketInsert.setInt(3, ticketCost);
            ticketInsert.setString(4, name);
            ticketInsert.setString(5, email);
            ticketInsert.executeUpdate(); //performs the insert on the table

            ticketInsert.close(); //closes the prepared statement

            conn.commit(); //commits the changes to the database

        } catch (Exception e1){
            e1.printStackTrace();

            try{
                conn.rollback(savepoint); //if there is an error, return to the savepoint before any database interactions (ensures that the database is intact)
            }catch(Exception e2){
                e2.printStackTrace();
            }
        }
        
    }










    public static String[][] task4(Connection conn, int gigID, String actName){
        Savepoint savepoint = null;
        try{
            conn.setAutoCommit(false); //sets autocommit to false to ensure databse integrity
            savepoint = conn.setSavepoint(); //saves the current state of the database
        } catch(Exception e0){ //if there is an error with either of these actions
            e0.printStackTrace();
            return null; //exits out of the method since database security cannot be assured
        }
        
        try{
            conn.setAutoCommit(false); //sets autocommit to false to ensure database integrity

            //checks whether the act is the headline act, if so we can cancel the gig straight away
            String checkHeadlineQuery = "SELECT MIN(intervaltonext) FROM gigLineup WHERE gigID = ? AND actname = ?"; //should return -1 if the act is the headline gig
            PreparedStatement checkHeadline = conn.prepareStatement(checkHeadlineQuery);
            checkHeadline.setInt(1, gigID);
            checkHeadline.setString(2, actName);

            ResultSet checkHeadlineRS  = checkHeadline.executeQuery();
            checkHeadlineRS.next(); //moves to the first row of the result
            int isHeadline = checkHeadlineRS.getInt(1); //gets the result from the query

            checkHeadlineRS.close();
            checkHeadline.close();

            if(isHeadline == -1){ //-1 indicates that this is the headline gig (hence we need to cancel the gig)
                String[][] affectedCustomers = cancelGig(conn, gigID); //calls the method to cancel the gig
                return affectedCustomers; //returns the string array of customers affected by the cancellation
            }

            //update and delete the acts according to the acts being deleted

            String updateActsQuery = 
                    "WITH " +
                        "actsBeingDeleted AS ( " + //gets the viewOrderingNum, gigID, actID, ontime and duration for the performances which need to be removed
                            "SELECT viewOrderingNum, gigID, actID, actstart AS ontime, duration " + //the required information
                            "FROM gigLineup " +
                            "WHERE gigID = ? AND actname = ? " + //those which are specified in the query
                        "), " +

                        "actsAffected AS ( " + //the other acts in the gig which are not being deleted
                            "SELECT gl.viewOrderingNum, gl.gigID, gl.actID, gl.actstart AS ontime, gl.duration " + //gets all necessary information about the acts
                            "FROM gigLineup gl " +
                            "WHERE gigID = ? " + //from the gig we're altering
                            "AND NOT EXISTS( " + //checks that it is not one of the gigs which we're deleting
                                "SELECT 1 " +
                                "FROM actsBeingDeleted abd " +
                                "WHERE gl.viewOrderingNum = abd.viewOrderingNum " + //viewOrderingNum is a unique identifier per gig
                            ") " +
                        ") " +

                    "UPDATE act_gig ag " + //updates on the act_gig table
                    "SET ontime = ontime - INTERVAL '1 minute' * COALESCE(( " + //alters the ontime by the duration of the previous deleted acts
                            "SELECT SUM(duration) " + //the total sum of durations
                            "FROM actsBeingDeleted abd " + //from the acts which are being deleted 
                            "WHERE abd.viewOrderingNum < (SELECT aa.viewOrderingNum FROM actsAffected aa WHERE aa.actID = ag.actID AND aa.ontime = ag.ontime)) " + //where the act being deleted is before the act affected
                            ",0) " + //if there are no acts being deleted before the act then we take away 0
                    "WHERE " +
                        "gigID = ? " + //where the gigID is the gigID we're altering
                        "AND EXISTS( " + //and that the act we're changing is one of the ones affected by the change 
                            "SELECT 1 " +
                            "FROM actsAffected aa " + //checks if the act is affected
                            "WHERE aa.actID = ag.actID " + //the acts are the same
                        ")";
                    
            PreparedStatement updateActs = conn.prepareStatement(updateActsQuery);
            updateActs.setInt(1, gigID);
            updateActs.setString(2, actName);
            updateActs.setInt(3, gigID);
            updateActs.setInt(4, gigID);

            String deleteActsQuery = 
                    "WITH " +
                        "actsBeingDeleted AS ( " + //gets the viewOrderingNum, gigID, actID, ontime and duration for the performances which need to be removed
                            "SELECT viewOrderingNum, gigID, actID, actstart AS ontime, duration " + //the required information
                            "FROM gigLineup " +
                            "WHERE gigID = ? AND actname = ? " + //those which are specified in the query
                        ") " +

                    "DELETE FROM act_gig " + //deletes the acts from act_gig which are
                    "WHERE (gigid, actID, ontime) IN (SELECT gigID, actID, ontime FROM actsBeingDeleted)"; //deletes the records matching the ones in actsBeingDeleted

            PreparedStatement deleteActs = conn.prepareStatement(deleteActsQuery);
            deleteActs.setInt(1, gigID);
            deleteActs.setString(2, actName);

            updateActs.executeUpdate(); //performs the query on the database
            deleteActs.executeUpdate(); //performs the delete on the database

            updateActs.close();
            deleteActs.close();

            try{
                actOverlapCheck(conn); //checks that there are no acts overlapping within the gig
                gigOverlapCheck(conn); //checks that there is no overlap between where an act is supposed to be
                actInterval(conn, gigID); //checks that the intervals between acts are still valid
                gigLength(conn, gigID); //checks thats the gig is still of a valid length
                gigStartMethod(conn, gigID); //checks that the first act and gig line up at a valid point 
                actTravel(conn, gigID); //checks that there is valid travel times for an act to get between gigs
                
            }catch(Exception e){ //if there is an error thrown by the checks (i.e. there is a business rule violated)
                e.printStackTrace(); //prints out the error
                conn.rollback(savepoint); //returns the database to the state it was in before it was altered
                String[][] affectedCustomers = cancelGig(conn, gigID); //calls the cancelGig method to cancel the gig and return the affected customers
                return affectedCustomers;
            }

            conn.commit(); //commits the changes to the database

            return task1(conn, gigID); //uses task1 to return the updated lineup for the gigID


        }catch(Exception e1){
            e1.printStackTrace();
            try{
                conn.rollback(savepoint);
            } catch(Exception e2){
                e2.printStackTrace();
            }
        }
        
        return null;
    }



    /**
     * This method performs the necessary operations when a gig is cancelled and returns the customers affected by the deletion
     * 
     * @param conn - the connection to the database
     * @param gigID - the gig that has been cancelled 
     * @return - a string[][] which contains all of the customers who've been affected by the cancellation
     */
    public static String[][] cancelGig(Connection conn, int gigID)throws Exception{
        try{
            String updateAfterCancelQuery = 
                    "UPDATE gig " + //updates the gig table
                    "SET gigstatus = 'C' " + //sets the gigstatus to C for cancelled
                    "WHERE gigID = ? ;" + //those entries where 

                    "UPDATE ticket " + //updates the ticket table
                    "SET cost = 0 " + //updates the cost in each ticket to -
                    "WHERE gigID = ? "; //for the tickets where the gigID matche


            PreparedStatement updateAfterCancel = conn.prepareStatement(updateAfterCancelQuery);
            updateAfterCancel.setInt(1, gigID);
            updateAfterCancel .setInt(2, gigID);

            updateAfterCancel.executeUpdate(); //updates the database

            //closes the preparedstatement
            updateAfterCancel.close();



            String getViolatedQuery = 
                "SELECT DISTINCT customername, customeremail " + //gets the unique customernames and customeremails
                "FROM ticket " + //from the tickets table
                "WHERE gigID = ? " + //where the tickets are for the deleted gig
                "ORDER BY customername ASC"; //orders the customers by their names alphabetically
            
            PreparedStatement getViolated = conn.prepareStatement(getViolatedQuery);
            getViolated.setInt(1, gigID);

            ResultSet violatedCustomersRS = getViolated.executeQuery();

            String[][] violatedCustomers = convertResultToStrings(violatedCustomersRS); //truns the result into a string

            //closes the resultset and preparedstatement
            violatedCustomersRS.close();
            getViolated.close();

            conn.commit(); //commits the changes to the database

            printTable(violatedCustomers);

            return violatedCustomers; //returns the violatedCustomers

        } catch(Exception e){
            e.printStackTrace();
            throw new Exception("Error occured whilst cancelling the gig.");
        }
    }










    public static String[][] task5(Connection conn){
        try{
            String ticketsToSellQuery = //the query which will get the ticket information
                "WITH gigInfo AS ( " +
                    "SELECT " +
                        "g.gigID, " +
                        "((SELECT SUM(actgigfee) FROM (SELECT DISTINCT actID, gigID, actgigfee FROM act_gig) WHERE gigID = g.gigID) + " + //the total cost of all distinct acts in the gig
                        "(SELECT hirecost FROM venue v WHERE v.venueID = g.venueID) " + //the cost of hiring the venue
                        ") AS totalCost, " +
                        
                        "(SELECT SUM(cost) FROM ticket WHERE gigID = g.gigID GROUP BY gigID) AS totalSoldValue, " + //the total value of all tickets sold
                        
                        "(SELECT MIN(price) FROM gig_ticket WHERE gigID = g.gigID) AS minTicketCost " + //the minimum ticket price for a gig
                    "FROM gig g " +
                ") " +

                "SELECT " +
                    "gi.gigID, " + //outputs the gigID
                    "CASE " +
                        "WHEN (SELECT COUNT(*) FROM ticket t WHERE t.gigID = gi.gigID) = 0 THEN CEIL(gi.totalCost / gi.minTicketCost) " + //if there are no tickets sold for a gig 
                        "ELSE CEIL( " + //rounds the result up (can't sell part of a ticket)
                                "GREATEST(gi.totalCost - gi.totalSoldValue, 0) " + // either selects how much money is left to cover the show or 0 if the ticket sales already cover the costs
                                " / gi.minTicketCost " + //divides by the minimum ticket cost (finds out how many need to be sold)
                            ") " +
                    "END AS TicketsToSell " +
                "FROM gigInfo gi;";
            
            PreparedStatement ticketsToSell = conn.prepareStatement(ticketsToSellQuery); //defines the prepared statement
            ResultSet ticketsToSellRS = ticketsToSell.executeQuery();

            String[][] result = convertResultToStrings(ticketsToSellRS); //converts the results into a String[][] array 

            ticketsToSell.close();
            ticketsToSellRS.close();

            return result;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }












    public static String[][] task6(Connection conn){
        try{
            String getTicketSalesQuery = 
                "WITH " +
                    "headlinedGigs AS ( " +
                        "SELECT actID, gigID " + //the headliner actID and gigID for a gig
                        "FROM giglineup " +
                        "WHERE intervaltonext = -1 " + // -1 indicates that an act is the final act for a specific gig
                    "), " + 

                    "ticketsSoldTab AS ( " +
                        "SELECT hg.actID, " + //the actID of the headliner of the gig
                            "COUNT(*) AS ticketsSold, " + //the number of tickets sold where that act is a headliner
                            "EXTRACT(YEAR FROM g.gigdatetime) AS gigYear " + //the year that the gig is taking place
                        "FROM ticket t " +
                        "JOIN headlinedGigs hg ON t.gigID = hg.gigID " +
                        "JOIN gig g ON g.gigID = t.gigID " + //the ticket is joined by gigID, therefore the number of rows for a gigID will be the amount of tickets per gig
                        "GROUP BY hg.actID, EXTRACT(YEAR FROM g.gigdatetime) " +  //groups the count by the actID and the year of the gig
                    "), " +

                    "namedSales AS ( " + //gets a table with the name of all the acts, the year, and the number of tickets sold from that act in that year
                        "SELECT a.actname, " +
                            "CAST(ts.gigYear AS TEXT) AS gigYear, " + //turns the gig year into a string
                            "ts.ticketsSold " +
                        "FROM act a " +
                        "JOIN ticketsSoldTab ts ON ts.actID = a.actID " +
                        "GROUP BY a.actname, ts.gigYear, ts.ticketsSold " + //groups the results by the actname and the year of the gig
                    "), " +

                    "totalSales AS ( " + //creates a table which has the name of the act, the gigYear as 'Total' and the sum of all the tickets sold for an act
                        "SELECT actname, " +
                            "'Total' AS gigYear, " + //sets the gigYear to total
                            "SUM(ticketsSold) AS ticketsSold " + //sums the amount of tickets sold by an act
                        "FROM namedSales " +
                        "GROUP BY actname " + //the amount of tickets sold is grouped by actname
                    "), " +

                    "results AS ( " +
                        "(SELECT * FROM namedSales) UNION ALL (SELECT * FROM totalSales ts) " + //unions the namedSales and totalSales tables together
                    ") " +

                "SELECT * FROM results " + //gets all values out of results table
                "GROUP BY actname, gigYear, ticketsSold " + //grouped by these attributes
                "ORDER BY SUM(ticketsSold) OVER (PARTITION BY actname), gigYear ASC"; //orders the acts by the ones with the most ticketsSold and puts the gigYears in order (this puts Total at the bottom and orders the years by their numerical values)
            
            PreparedStatement getTicketSales = conn.prepareStatement(getTicketSalesQuery);
            ResultSet getTicketSalesRS = getTicketSales.executeQuery();

            String[][] result = convertResultToStrings(getTicketSalesRS);

            getTicketSalesRS.close();
            getTicketSales.close();

            return result;

        } catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }











    public static String[][] task7(Connection conn){
        try{
            String regularCustomersQuery = 
                "WITH " +
                    "customerAttendance AS ( " + //gets a table of all the tickets to headline acts that a customer has
                        "SELECT " +
                            "t.customername, " + //the name of the customer
                            "t.customeremail, " + //the email of the customer
                            "gl.actname, " + //the name of the act they attended
                            "gl.actID, " + //the actID which they attended
                            "EXTRACT(YEAR FROM gl.actstart) AS actYear " + //the year of the performance 
                        "FROM ticket t " +
                        "JOIN gigLineup gl ON t.gigID = gl.gigID " +
                        "WHERE gl.intervaltonext = -1 " + //where the act is the headline act(denoted by -1)
                    "), " +

                    "actHeadlines AS ( " + //stores an array of all the years an act has performed a headline gig
                        "SELECT actID, actname, " + //the ID and name of the act in question
                            "ARRAY_AGG(DISTINCT EXTRACT(YEAR FROM actstart) ORDER BY EXTRACT(YEAR FROM actstart)) AS yearsPerformed " + //the array of years an act has performed a headline
                        "FROM gigLineup " +
                        "WHERE intervaltonext = -1 " + //only selects acts which are the headline act (denoted by -1)
                        "GROUP BY actID, actname " + //grouped together for each individual act
                    "), " +

                    "customerHeadlines AS ( " + //stores a table with the the years where a customer has attended an act headline
                        "SELECT customername, " + //the name of the customer
                            "customeremail, " + //the email of the customer
                            "actID, " + //the id of the act they attended
                            "actname, " + //the name of the act they attended
                            "ARRAY_AGG(DISTINCT actYear ORDER BY actYear) AS attendedYears " + //an array of each year the customer has attended a gig where the act is the headline
                        "FROM customerAttendance " +
                        "GROUP BY actID, actname, customername, customeremail " + //groups this based on the act and the specific customer
                    "), " +

                    "regularCustomers AS ( " + //stores a table of customers which are considered loyal to a specific act
                        "SELECT ch.customername, " + //the customers name
                            "ch.customeremail, " + //the customers email
                            "ch.actname, " + //the name of the act
                            "ch.actID, " + //the ID of the act
                            "(SELECT COUNT(*) " + //gets the number of tickets that a customer has for a gig where this act has headlines
                                "FROM customerAttendance ca " +
                                "WHERE ca.customername = ch.customername " + //the name of the customers match
                                "AND ca.customeremail = ch.customeremail " + //the email of the customers match
                                "AND ca.actID = ch.actID " + //the act they attended matches
                            ") AS ticketNum " + //stores it as the ticketNum
                        "FROM customerHeadlines ch " +
                        "JOIN actHeadlines ah ON ch.actID = ah.actID " + //joins the customerheadlines table to the actheadlines table
                        "WHERE ch.attendedYears = ah.yearsPerformed " + //where the customer has attended gigs in the exact same years that the act has performed a headline
                    ") " +

                "SELECT " +
                    "a.actname, " + //gets the name of the act
                    "COALESCE(rc.customername, '[None]') AS regularCustomers " + //stores the loyalcustomer as one from the loyalcustomer table or if none exist as [None]
                "FROM act a " +
                "LEFT JOIN regularCustomers rc ON a.actID = rc.actID " + //uses left join to get every act even if it's not present in loyalCustomers
                "WHERE (SELECT ah.yearsPerformed FROM actHeadlines ah WHERE ah.actID = a.actID) <> '{}' " + //only for those acts which have had a headline performance
                "ORDER BY a.actname, rc.ticketNum DESC"; //orders it by the actname and the amount of tickets bought by the customer
            
            PreparedStatement regularCustomers = conn.prepareStatement(regularCustomersQuery);
            ResultSet regularCustomersRS = regularCustomers.executeQuery();

            String[][] result = convertResultToStrings(regularCustomersRS);

            regularCustomersRS.close();
            regularCustomers.close();

            return result;

        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }











    public static String[][] task8(Connection conn){
        try{
            String economicallyFeasibleGigsQuery= 
                    "WITH " +
                        "avgTicketPriceTab AS ( " + //gets the average ticket price of all tickets in the database
                            "SELECT ROUND(AVG(cost)) AS avgTicketPrice " + //gets the average cost from ticket and rounds it to the nearest integer
                            "FROM ticket t " +
                            "WHERE (SELECT gigstatus FROM gig g WHERE g.gigID = t.gigID) <> 'C' " + //only considers the tickets for gigs which haven't been cancelled
                        "), " +

                        "gigVenuePairCost AS ( " + //gets the cost of a pair of act and venue
                            "SELECT a.actname, v.venuename, " +
                                "((SELECT standardfee FROM act WHERE actname = a.actname) " + //gets the standard fee of a given act
                                "+ (SELECT hirecost FROM venue WHERE v.hirecost = hirecost)) AS totalCost " + //gets the hire cost of a given venue and adds the two together
                            "FROM act a CROSS JOIN venue v " + //from every combination of act and venue
                        "), " +

                        "ticketSales AS ( " + //gets the number of tickets required to break even for a gig venue pair
                            "SELECT gvp.venuename, gvp.actname, " +
                                "CEIL(gvp.totalCost / (SELECT avgTicketPrice FROM avgTicketPriceTab)) AS numberOfTickets " + //gets the rounded number of tickets for the average ticket cost to cover all costs
                            "FROM gigVenuePairCost gvp " +
                        ") " +

                    "SELECT ts.venuename, ts.actname, ts.numberOfTickets " + //selects the required information
                    "FROM ticketSales ts " +
                    "WHERE ts.numberOfTickets <= (SELECT DISTINCT capacity FROM venue v WHERE v.venuename = ts.venuename) " + //where the number of tickets required to break even is viable for the venue size
                    "ORDER BY ts.venuename, ts.numberOfTickets DESC;"; //orders the results by the venue names and then the number of tickets required
        
            PreparedStatement economicallyFeasibleGigs = conn.prepareStatement(economicallyFeasibleGigsQuery);
            ResultSet economicallyFeasibleGigsRS = economicallyFeasibleGigs.executeQuery();

            String[][] result = convertResultToStrings(economicallyFeasibleGigsRS);

            printTable(result);

            economicallyFeasibleGigsRS.close();
            economicallyFeasibleGigs.close();
            
            return result;

        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }




    //BELOW ARE A SELECTION OF CHECKS WHICH WILL BE PERFORMED ON THE DATABASE TO ENSURE INTREGRITY WITH THE BUSINESS RULES





    /**
     * This function intakes a gigID and performs an sql query to determine whether or not the final act of this gig finishes > 60 minutes after the start of the gig.
     * 
     * @param conn - the connection to the database
     * @param gigID - the ID of the gig which we're checking the length for
     * @throws Exception - an exception that is thrown if the length of the gig is invalid
     */
    private static void gigLength(Connection conn, int gigID) throws Exception{ 
        //checks that the gig lasts for at least 60 minutes
        String gigLengthCheckQuery = 
        "WITH " +
            "lastAct AS ( " + //gets the viewOrderingNum of the act which starts the latest in the gigID
                "SELECT MAX(viewOrderingNum) AS viewOrdNum " +
                "FROM giglineup gl " +
                "WHERE gl.gigID = ? " +
            "), " +

            "post60Time AS ( " + //gets the gigdatetime of the gigID and adds 60 minutes to it
                "SELECT gigdatetime + INTERVAL '60 minutes' AS post60 " +
                "FROM gig g " +
                "WHERE g.gigID = ? " +
            "), " +

            "lastActTime AS ( " + //gets the actend of the latest act in the gig
                "SELECT actend " +
                "FROM giglineup gl " +
                "WHERE gl.gigID = ? " +
                "AND gl.viewOrderingNum = (SELECT viewOrdNum FROM lastAct) " +
            ") " +

        "SELECT " +
            "CASE " + //checks that the act finishes more than 60 minutes after the start
                "WHEN (SELECT post60 FROM post60Time) > (SELECT actend FROM lastActTime) THEN 1 " + //if the act finishes before 60 minutes later select 1
                "ELSE 0 " + //otherwise if the act finishes at a valid time, select 0
            "END";

        //creates the prepared statement and inputs the parameters
        PreparedStatement gigLengthCheck = conn.prepareStatement(gigLengthCheckQuery);
        gigLengthCheck.setInt(1, gigID);
        gigLengthCheck.setInt(2, gigID);
        gigLengthCheck.setInt(3, gigID);

        //retrieves the results from the gigLengthCheck query
        ResultSet validLength = gigLengthCheck.executeQuery();
        validLength.next(); //moves to the first return of the resultset
        int shortGigNum = validLength.getInt(1); //the number returned from the query

        //closes the preparedStatement and Resultset
        validLength.close();
        gigLengthCheck.close();

        if(shortGigNum==1){ //if there is a gig which is too short
            throw new Exception("The gig lasts less than 60 minutes, this is not a valid gig.");
        }
    }








    /**
     * This method intakes a gigID and checks whether the first act of that gig is at the same time as the start of the gig.
     * If it is not at the same time then this violates the database constraints and an exception is thrown.
     * 
     * @param conn - the connection to the database
     * @param gigID - the ID of the gig which we're checking
     * @throws Exception - an exception thrown if start of the gig is not a valid
     */
    private static void gigStartMethod(Connection conn, int gigID) throws Exception{
        //checks whether the earliest act for a gig starts at the same time as the gig
        String actGigStartCheckQuery = 
            "SELECT COUNT(*) FROM giglineup gl " + //selects the number of rows from giglineup which satisfy the condition
            "WHERE gigID = ? " + //only those acts which are from the gig we're checking
            "AND viewOrderingNum = 1 " + //the first act in the gig
            "AND actstart = (SELECT gigdatetime FROM gig WHERE gigID = ?)"; //the start of the act is the same as the gigdatetime of the gig with the matching ID

        //creates the prepared statement and inputs the parameters
        PreparedStatement actGigStartCheck = conn.prepareStatement(actGigStartCheckQuery);
        actGigStartCheck.setInt(1, gigID);
        actGigStartCheck.setInt(2, gigID);

        //retrieves the results from the gigEndCheck query
        ResultSet validStart = actGigStartCheck.executeQuery();
        validStart.next(); //moves to the first return of the resultset
        int matchingStartNum = validStart.getInt(1); //the number of gigStart and actStart matchings (should be 1)

        //closes the preparedStatement and Resultset
        validStart.close();
        actGigStartCheck.close();

        if(matchingStartNum != 1){
            throw new Exception("The first act does not play at the same time as the start of the gig, this is invalid.");
        }
    }








    /**
     * This method checks that once all acts to a gig have been insterted that the gig ends before 11pm if there are "loud" acts
     * or that the gig ends before 1am if there are no "loud" acts.
     * 
     * @param conn - the connection to the database
     * @param gigID - the ID of the gig which we're checking is valid
     * @param gigStart - the time that the gigStarts at 
     * @throws Exception - an exception thrown if the condition is found to be invalid
     */
    private static void gigEnd(Connection conn, int gigID, LocalDateTime gigStart) throws Exception{
        //checks whether this gig is considered loud because it has a rock or pop act in its lineup
        String gigEndCheckQuery =
                "WITH " +
                    "gigEndTime AS ( " + //defines gigEndTime as the final time 
                        "SELECT gigend " +
                        "FROM venueLineup " +
                        "WHERE gigID = ? " +
                    "), " +

                    "loudGenresCount AS ( " + //gets the number of acts within the given gigID which are considered loud
                        "SELECT COUNT(*) AS loudActCount " +
                        "FROM gigLineup " +
                        "WHERE gigID = ? " +
                        "AND (genre = 'Rock' OR genre = 'Pop') " +
                    ") " +
                "SELECT " +
                    "CASE " + //checks whether either of the invalid conditions hold
                        "WHEN (SELECT gigend FROM gigEndTime) > DATE(?) + INTERVAL '23:00:00' " + //the end of the gig is greater than 11pm on the same day as the start of the gig
                            "AND (SELECT loudActCount FROM loudGenresCount) >= 1 THEN 1 " + //and there is a loud act within the gig then return 1
                        "WHEN (SELECT gigend FROM gigEndTime) > DATE(?) + INTERVAL '1 day' + INTERVAL '01:00:00' " + //the end of the gig is greater than 1am the day after the start of the gig
                            "AND (SELECT loudActCount FROM loudGenresCount) = 0 THEN 1 " + //and there were no loud acts within the gig then return 1
                        "ELSE 0 " + //otherwise return 0 (the finish time is valid)
                    "END";

        //creates the prepared statement and inputs the parameters
        PreparedStatement gigEndCheck = conn.prepareStatement(gigEndCheckQuery);
        gigEndCheck.setInt(1, gigID);
        gigEndCheck.setInt(2, gigID);
        gigEndCheck.setTimestamp(3, Timestamp.valueOf(gigStart));
        gigEndCheck.setTimestamp(4, Timestamp.valueOf(gigStart));

        //retrieves the results from the gigEndCheck query
        ResultSet validEnd = gigEndCheck.executeQuery();
        validEnd.next(); //moves to the first return of the resultset
        int invalidEndNum = validEnd.getInt(1); //the number returned from the query

        //closes the preparedStatement and Resultset
        validEnd.close();
        gigEndCheck.close();

        //checks the boolean value of invalidEndNum
        if (invalidEndNum > 0){
            throw new Exception("The gig finishes too late, this gig is invalid"); //throws an exception if the gig finshes too late
        }
    }









    /**
     * This method is used to check that all acts which've just been altered for a specific gig have enough time to travel to/from different performances
     *
     * @param conn - the connection to the database
     * @param gigID - the gig we've altered information for
     * @throws Exception - an exception thrown if acts do not have sufficient time to travel between gigs
     */
    private static void actTravel(Connection conn, int gigID) throws Exception{
        //checking an act has enough time to travel to the next gig after it has performed
        String checkActTravelAfterQuery =
                "WITH nextAct AS ( " +
                    "SELECT " +
                        "actID, gigID, intervaltonext, " + //gets the necessary information from actLineup (actID is used for ordering)
                        "LEAD(gigID) OVER (PARTITION BY actID ORDER BY actstart) AS nextGigID " + //the gigID after the current one in actLineup
                    "FROM actLineup ) " +

                "SELECT COUNT(*) FROM nextAct " + //gets the number of rows which satisfy this condition
                "WHERE gigID = ? " + //considers those acts from the gig we've just entered
                "AND intervaltonext < 60 " + //where the interval to the next gig for the act is less than 60 (not enough travel time)
                "AND intervaltonext <> -1 " + //-1 is used to indicate that it is the acts final known performance and hence we need to exclude these acts
                "AND nextGigID <> ? "; //the performance after the current one is at a different gig (and hence they need to travel)
        
        PreparedStatement checkActTravelAfter = conn.prepareStatement(checkActTravelAfterQuery);
        checkActTravelAfter.setInt(1, gigID);
        checkActTravelAfter.setInt(2, gigID);

        //retrieves the result from the prepared statement
        ResultSet invalidTravelAfter = checkActTravelAfter.executeQuery();
        invalidTravelAfter.next(); //moves the pointer
        int invalidTravelAfterNum = invalidTravelAfter.getInt(1); //stores the result of the query

        //close connection to the resultset and preparedstatement
        invalidTravelAfter.close();
        checkActTravelAfter.close();

        if(invalidTravelAfterNum > 0){
            throw new Exception("An entered act does not have sufficient time to travel to/from gig " + gigID);
        }



        //checking an act has enough time to travel from the previous gig before it performs
        String checkActTravelBeforeQuery =
                "WITH previousAct AS ( " +
                    "SELECT " +
                        "actID, gigID, intervaltonext, " +
                        "LEAD(gigID) OVER (PARTITION BY actID ORDER BY actstart) AS previousGigID " +
                    "FROM actLineup ) " +

                "SELECT COUNT(*) FROM previousAct " + //gets the number of rows that satisfy this condition
                "WHERE previousGigID = ? " + //the next gigID from this row is the gigID of the gig we've just entered
                "AND intervaltonext < 60 " + //the interval from the previous gig for this act is less than 60 minutes
                "AND intervaltonext >= 0 " + //-1 is used to indicate that it is the acts final known performance and hence we need to exclude these acts
                "AND gigID <> ?"; //the performance before the current one is at a different gig (and hence they need to travel)

        PreparedStatement checkActTravelBefore = conn.prepareStatement(checkActTravelBeforeQuery);
        checkActTravelBefore.setInt(1, gigID);
        checkActTravelBefore.setInt(2, gigID);

        //retrieves the result from the prepared statement
        ResultSet invalidTravelBefore = checkActTravelBefore.executeQuery();
        invalidTravelBefore.next(); //moves the pointer
        int invalidTravelBeforeNum = invalidTravelBefore.getInt(1); //stores the result of the query

        //close connection to the resultset and preparedstatement
        invalidTravelBefore.close();
        checkActTravelBefore.close();

        if(invalidTravelBeforeNum > 0){
            throw new Exception("An entered act does not have sufficient time to travel from their previous gig to gig " + gigID);
        }

    }









    /**
     * This method checks that for the gigID which has just been altered that all acts within the gig have valid intervals between them
     * 
     * @param conn - the connection the the database
     * @param gigID - the gigID of the gig we've altered
     * @throws Exception - an exception which will be returned if the act intervals are not valid
     */
    private static void actInterval(Connection conn, int gigID) throws Exception{
        String checkActIntervalQuery =
            "WITH actInterval AS ( " +
                "SELECT gigID, intervaltonext " + //gets the necessary information from the gigLineup view
                "FROM gigLineup ) " +

            "SELECT COUNT(*) " + //the number that satisfy the conditions
            "FROM actInterval " +
            "WHERE gigID = ? " + //the acts which are from the gig we've entered
            "AND NOT ((intervaltonext BETWEEN 15 AND 30) " + //and the intervals are not between 15 and 30
                "OR (intervaltonext = -1) " + //or the interval is not -1 (-1 indicates end of the gig)
                "OR (intervaltonext = 0))";  //or the interval is 0 (acts scheduled directly one after another)
        
        PreparedStatement checkActInterval = conn.prepareStatement(checkActIntervalQuery);
        checkActInterval.setInt(1, gigID);

        ResultSet actIntervalRS = checkActInterval.executeQuery();
        actIntervalRS.next();
        int invalidIntervalCount = actIntervalRS.getInt(1);

        actIntervalRS.close();
        checkActInterval.close();

        if(invalidIntervalCount>0){
            throw new Exception("The lineup of act has invalid intervals.");
        }

    }










    /**
     * This method checks that for the gigID which has been altered, that the venue where it is being performed at, has sufficient time to turnover from any adjacent gigs
     * 
     * @param conn - the connection to the database
     * @param gigID - the ID of the gig which has been added
     * @throws Exception - the exception which will be thrown if there is a clash between gigs
     */
    private static void venueTurnoverCheck(Connection conn, int gigID)throws Exception{
        String checkVenueTurnoverQuery = 
            "WITH " +
                "venue AS ( " + //gets the ID of the venue where the gig is being played
                    "SELECT venueID " +
                    "FROM gig " +
                    "WHERE gigID = ? " +
                "), " +

                "venueInterval AS ( " + //gets the necessary information about the venue lineup from venueLineup
                    "SELECT venueID, intervaltonext " +
                    "FROM venueLineup " +
                ") " +
            
            "SELECT COUNT(*) " + //gets the number where this is true
            "FROM venueInterval " + //from the established cte
            "WHERE venueID = (SELECT venueID FROM venue) " + //only those which have the venueID we're looking for
            "AND intervaltonext <> -1 " + //-1 is used to indicate the last gig for the venue, we don't need to worry about this
            "AND intervaltonext < 180"; //finds the turnovers which are less than 180 minutes
        
        //queries the database
        PreparedStatement checkVenueTurnover = conn.prepareStatement(checkVenueTurnoverQuery);
        checkVenueTurnover.setInt(1, gigID);

        //stores the result as a resultset and gets the information from it
        ResultSet venueTurnoverRS = checkVenueTurnover.executeQuery();
        venueTurnoverRS.next();
        int invalidVenueTurnoverNum = venueTurnoverRS.getInt(1);

        //closes the preapred statement and resultset
        venueTurnoverRS.close();
        checkVenueTurnover.close();

        //checks whether there were any invalid venue turnovers
        if(invalidVenueTurnoverNum>0){
            throw new Exception("The venues do not have enough time to turnover between gigs for gig "+gigID);
        }

    }










    /**
     * This method queries the database to check whether there are any instances of overlap between two gigs at the same venue.
     * 
     * @param conn - the connection to the database
     * @throws Exception - returns an exception if any instances of this is found
     */
    private static void venueGigOverlapCheck(Connection conn)throws Exception{
        String checkVenueOverlapQuery = //checking whether a venue has two gigs overlapping
        "WITH gigsOverlap AS ( " +
            "SELECT vl1.gigID, vl2.gigID, vl2.venueID " + //selects arbitrary result
            "FROM venueLineup vl1 " +
            "JOIN venueLineup vl2 ON vl1.venueID = vl2.venueID " + //connects two instances of the venueLineup view together by venueID (allows us to compare every pair of gigs at the same venue)
            "WHERE vl1.gigID <> vl2.gigID " + //excludes the same gig
            "AND ( " +
                "(vl1.viewOrderingNum < vl2.viewOrderingNum AND vl1.gigend > vl2.gigstart) " + //if vl1 is before vl2 and vl1 runs into vl2's time
                "OR " +
                "(vl1.viewOrderingNum > vl2.viewOrderingNum AND vl2.gigend > vl1.gigstart) " + //if vl1 is after vl2 and vl2 runs into vl1's time
                "OR " +
                "(vl1.gigstart = vl2.gigstart OR vl1.gigend = vl2.gigend) " + //if the two gigs start or finish at the same time
            ") " +
        ") " +
        "SELECT COUNT(*) " + //gets the number of instances where this occurs
        "FROM gigsOverlap";

        //queries the database
        PreparedStatement checkVenueOverlap = conn.prepareStatement(checkVenueOverlapQuery);

        //stores the result as a resultset and gets the information from it
        ResultSet venueOverlapRS = checkVenueOverlap.executeQuery();
        venueOverlapRS.next();
        int invalidVenueOverlapNum = venueOverlapRS.getInt(1);

        //closes the preapred statement and resultset
        venueOverlapRS.close();
        checkVenueOverlap.close();

        //checks whether there were any invalid venue turnovers
        if(invalidVenueOverlapNum>0){
            throw new Exception("There is an overlap of gigs at the venue.");
        }
    }
    









    /**
     * This method queries the database to check whether there are any instances of an act being scheduled in two gigs at the same time.
     * 
     * @param conn - the connection to the database
     * @throws Exception - returns an exception if any instance of this found
     */
    private static void gigOverlapCheck(Connection conn)throws Exception{
        String checkGigOverlapQuery = //checking whether an act has two performances at different gigs which overlap
        "WITH " +
            "gigsOverlap AS ( " +
                "SELECT al1.gigID, al2.gigID, al1.actID " + //selects arbitary results
                "FROM actLineup al1 " +
                "JOIN actLineup al2 ON al1.actID = al2.actID " + //connects two instances of the actLineup view together by the actIDs (allows us to compare every pair of performances of an act)
                "WHERE (al1.gigID <> al2.gigID OR al1.actstart <> al2.actstart) " + //excludes the exact same performance
                "AND ( " +
                    "(al1.viewOrderingNum < al2.viewOrderingNum AND al1.actend > al2.actstart) " +  //if al1 is before al2 and runs into al2's time
                    "OR " +
                    "(al1.viewOrderingNum > al2.viewOrderingNum AND al2.actend > al1.actstart) " + //if al1 is after al2 and al2 runs into al1's time
                    "OR " +
                    "(al1.actstart = al2.actstart OR al1.actend = al2.actend) " + //if the two acts start or finish at the same time
                ") " +
        ") " +
        "SELECT COUNT(*) " + //gets the number of instances where this occurs
        "FROM gigsOverlap";

        //queries the database
        PreparedStatement checkGigOverlap = conn.prepareStatement(checkGigOverlapQuery);

        //stores the result as a resultset and gets the information from it
        ResultSet gigOverlapRS = checkGigOverlap.executeQuery();
        gigOverlapRS.next();
        int invalidGigOverlapNum = gigOverlapRS.getInt(1);

        //closes the preapred statement and resultset
        gigOverlapRS.close();
        checkGigOverlap.close();

        //checks whether there were any invalid venue turnovers
        if(invalidGigOverlapNum>0){
            throw new Exception("An act is scheduled to perform at two gigs simultaneously, this is invalid.");
        }
    }
    
    








    /**
     * This method queries the database for any instances where there are two acts overlapping within the same gig.
     * 
     * @param conn - the connection to the database
     * @throws Exception - throws an exception if any instance of this is found
     */
    private static void actOverlapCheck(Connection conn)throws Exception{
        String checkActOverlapQuery = //checking whether an acts overlap within a gig
        "WITH " +
            "actsOverlap AS ( " +
                "SELECT gl1.actID, gl2.actID, gl1.gigID " +
                "FROM gigLineup gl1 " +
                "JOIN gigLineup gl2 ON gl1.gigID = gl2.gigID " + //connects two instances of the gigLineup view together by gigIDs (allows us to compare very pair of performances at the same gig)
                "WHERE (gl1.actID <> gl2.actID OR gl1.actstart <> gl2.actstart) " + //excludes the exact same performance
                "AND ( " +
                    "(gl1.viewOrderingNum < gl2.viewOrderingNum AND gl1.actend > gl2.actstart) " + //if gl1 is before gl2 and runs into gl2's time
                    "OR " +
                    "(gl1.viewOrderingNum > gl2.viewOrderingNum AND gl2.actend > gl1.actstart) " + //if gl1 is after gl2 and gl2 runs into al1's time
                    "OR " +
                    "(gl1.actstart = gl2.actstart OR gl1.actend = gl2.actend) " + //if the two acts start or finish at the same time
                ") " +
        ") " +
        "SELECT COUNT(*) " + //gets the number of instances where this occurs
        "FROM actsOverlap";

        //queries the database
        PreparedStatement checkActOverlap = conn.prepareStatement(checkActOverlapQuery);

        //stores the result as a resultset and gets the information from it
        ResultSet actOverlapRS = checkActOverlap.executeQuery();
        actOverlapRS.next();
        int invalidActOverlapNum = actOverlapRS.getInt(1);

        //closes the preapred statement and resultset
        actOverlapRS.close();
        checkActOverlap.close();

        //checks whether there were any invalid venue turnovers
        if(invalidActOverlapNum>0){
            throw new Exception("Two acts are scheduled to perform at the same time within the gig, this is invalid.");
        }
    }
    
                
                


    /**
     * Prompts the user for input
     * @param prompt Prompt for user input
     * @return the text the user typed
     */
    private static String readEntry(String prompt) {
        
        try {
            StringBuffer buffer = new StringBuffer();
            System.out.print(prompt);
            System.out.flush();
            int c = System.in.read();
            while(c != '\n' && c != -1) {
                buffer.append((char)c);
                c = System.in.read();
            }
            return buffer.toString().trim();
        } catch (IOException e) {
            return "";
        }

    }
     
    /**
    * Gets the connection to the database using the Postgres driver, connecting via unix sockets
    * @return A JDBC Connection object
    */
    public static Connection getSocketConnection(){
        Properties props = new Properties();
        props.setProperty("socketFactory", "org.newsclub.net.unix.AFUNIXSocketFactory$FactoryArg");
        props.setProperty("socketFactoryArg",System.getenv("HOME") + "/cs258-postgres/postgres/tmp/.s.PGSQL.5432");
        Connection conn;
        try{
          conn = DriverManager.getConnection("jdbc:postgresql://localhost/cwk", props);
          return conn;
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gets the connection to the database using the Postgres driver, connecting via TCP/IP port
     * @return A JDBC Connection object
     */
    public static Connection getPortConnection() {
        
        String user = "postgres";
        String passwrd = "password";
        Connection conn;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException x) {
            System.out.println("Driver could not be loaded");
        }

        try {
            conn = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/cwk?user="+ user +"&password=" + passwrd);
            return conn;
        } catch(SQLException e) {
            System.err.format("SQL State: %s\n%s\n", e.getSQLState(), e.getMessage());
            e.printStackTrace();
            System.out.println("Error retrieving connection");
            return null;
        }
    }

    /**
     * Iterates through a ResultSet and converts to a 2D Array of Strings
     * @param rs JDBC ResultSet
     * @return 2D Array of Strings
     */
     public static String[][] convertResultToStrings(ResultSet rs) {
        List<String[]> output = new ArrayList<>();
        String[][] out = null;
        try {
            int columns = rs.getMetaData().getColumnCount();
            while (rs.next()) {
                String[] thisRow = new String[columns];
                for (int i = 0; i < columns; i++) {
                    thisRow[i] = rs.getString(i + 1);
                }
                output.add(thisRow);
            }
            out = new String[output.size()][columns];
            for (int i = 0; i < output.size(); i++) {
                out[i] = output.get(i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }

    public static void printTable(String[][] out){
        int numCols = out[0].length;
        int w = 20;
        int widths[] = new int[numCols];
        for(int i = 0; i < numCols; i++){
            widths[i] = w;
        }
        printTable(out,widths);
    }

    public static void printTable(String[][] out, int[] widths){
        for(int i = 0; i < out.length; i++){
            for(int j = 0; j < out[i].length; j++){
                System.out.format("%"+widths[j]+"s",out[i][j]);
                if(j < out[i].length - 1){
                    System.out.print(",");
                }
            }
            System.out.println();
        }
    }

}
