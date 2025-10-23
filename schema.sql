--TABLE DEFINITIONS

/*
This table holds tuples which represent the information for a specific act.
    - Each act has an primary key actID which is of a serial type
    - The act has a unique name which is no longer than 100 characters, this name must be present
    - The genre of the act is stored as a string of no more than 10 characters, this must be present
    - The standard fee which is a non-negative integer
*/
DROP TABLE IF EXISTS act CASCADE; --drops the table if it already exists
CREATE TABLE act ( --creates the table for the act information
    actID SERIAL PRIMARY KEY, --the primary key, as a serial type
    actname VARCHAR(100) NOT NULL, --defines the actname as a string with max length 100
    genre VARCHAR(10) NOT NULL, --defines the genre as a string with max length 10
    standardfee INTEGER CHECK (standardfee >= 0) NOT NULL, --defines the standard fee of the act

    UNIQUE(actname) --all of the act names must be unique from each other
);


/*
This is the table which stores the information about a specific venue
    - The venueID is the primary key, to represent each venue
    - The venuename is a string no longer than 100 characters. Each venue name must be unique
    - The hirecost is the overall cost of using the venue for a gig, it cannot be negative and must be present
    - The capacity is an integer which stores the amount of tickets which can be sold for a venue. The value must be present
*/
DROP TABLE IF EXISTS venue CASCADE;
CREATE TABLE venue (
    venueID SERIAL PRIMARY KEY, --the unique ID for the venue as the primary key
    venuename VARCHAR(100) NOT NULL, --the name of the venue up to 100 characters
    hirecost INTEGER CHECK (hirecost >= 0) NOT NULL, --the cost for hiring the venue, this must be known and not negative
    capacity INTEGER NOT NULL, --the capacity of the venue, it is safety critical that this is known
    
    UNIQUE(venuename) --the venue name should always be unique
);

/*
This table holds information about each gig
    - Each gig has a gigID which is of type serial and is the primary key
    - Each gig is held at a specific venue referenced as the venueID which links to the venue table
    - The name for the gig is stored as the gigtitle, a string of no more than 100 characters. This value must be present
    - The start time of the gig is stored as the gigdatetime, using a timestamp type. This value must be present and between 9am and 11:59pm
    - The status of the gig is a single character which is either; 'C' = cancelled or 'G' = Going ahead
    - The venueID and gigdate time are unique as there cannot be two gigs at the same starttime within the same venue
*/
DROP TABLE IF EXISTS gig CASCADE; --drops the table if it already exists
CREATE TABLE gig (
    gigID SERIAL PRIMARY KEY, --defines the primary key, as a serial type
    venueID INTEGER NOT NULL, --defines a relation to the venue where it will be held as a foreign key
    gigtitle VARCHAR(100) NOT NULL, --defines the title for the gig (it cannot be null)
    gigdatetime TIMESTAMP CHECK (gigdatetime :: TIME BETWEEN '09:00:00' AND '23:59:00') NOT NULL, --defines the date/time for the gig (it cannot be null) and must be within a certain time range
    gigstatus VARCHAR(1) CHECK (gigstatus IN ('C','G')) NOT NULL, --defines the gigstatus as a character of either 'C' or 'G'

    FOREIGN KEY (venueID) REFERENCES venue (venueID) ON DELETE CASCADE --a foreign key related to the venue table (if the venue is deleted then this gig is deleted)
);


/*
This table holds the information about a specific instance of an act performing in a gig
    - The actID references the act table for which act is performing within this gig (is a foreign key)
    - The gigID references the gig table for which gig the act is performing at (is a foreign key)
    - The actgigfee is the fee that the act will charge for their performance at the specific gig, this must be non-negative and present
    - The ontime is the time at which the act will start performing at the gig, the is represented as a timestamp. This value must be present
    - The duration is how long this specific act will be performing for at the gig
    - The primary key is the unique combination of the actID, gigID and ontime.
*/
DROP TABLE IF EXISTS act_gig CASCADE; --drops table if it already exists
CREATE TABLE act_gig ( --creates the act_gig table
    actID INTEGER NOT NULL, --defines the actID as an integer used to link to the act table
    gigID INTEGER NOT NULL, --defines the gigIF as an integer used to link to the gig table
    actgigfee INTEGER CHECK (actgigfee >= 0) NOT NULL, --ensures that the fee is not negative
    ontime TIMESTAMP NOT NULL, --the time at which the act will start at this particular gig
    duration INTEGER CHECK (duration >= 15 AND duration <= 90) NOT NULL, --how long the performance lasts in minutes, it must be within a specific time range

    PRIMARY KEY (actID, gigID, ontime), --defines the primary key as the unique combination of the actID, gigID and the ontime (since the same act can have different ontimes)
    FOREIGN KEY (actID) REFERENCES act (actID) ON DELETE CASCADE, --a foreign key related to the act table (if the act is deleted then this act_gig is deleted)
    FOREIGN KEY (gigID) REFERENCES gig (gigID) ON DELETE CASCADE --a foreign key related to the gig table (if the gig is deleted then this act_gig is deleted)
);


/*
This is the table which stores the information about the types of tickets sold for each gig
    - The gigID is the ID referencing the specific gig that the ticket is used for, stored as an integer
    - The pricetype is the type of ticket that this is, it is repsented by a two charavter string, this value must be present
    - The price is the cost of this ticket type, it is an non-negative integer, and must be present
    - The primary key for this table is the unique combination of the gigID and pricetype
*/
DROP TABLE IF EXISTS gig_ticket CASCADE;
CREATE TABLE gig_ticket (
    gigID INTEGER NOT NULL, --the gig that the ticket is for
    pricetype VARCHAR(2) NOT NULL, --the type of ticket
    price INTEGER CHECK (price >=0 AND NOT(pricetype = 'A' AND price = 0)) NOT NULL, --defines the price as an integer which is non negative
    --this further ensures that if the ticket is of type Adult ('A') that it cannot be 0

    PRIMARY KEY (gigID, pricetype), --the primary key is defined by the combination of the gigID and the pricetype
    FOREIGN KEY (gigID) REFERENCES gig (gigID) ON DELETE CASCADE --a foreign key related to the gig table (if the gig is deleted then delete the gig_ticket)
);


/*
This is the table which represents the each individual ticket sold
    - The ticketID represents the primary key for each ticket sold, this is a serial type
    - The gigID represents the reference to the gig which this ticket is for (it links to the gig_ticket which links to the specific gig). This value must be present
    - The pricetype is the type of ticket this is and is a reference from the gig_ticket table. This value must be present
    - The cost is the price paid for the ticket and cannot negative.  This value must be present
    - The customername is stored as a string of max 100 charaters. This value must be present
    - The customeremail is stored as a string of max 100 characters. This value must be present
*/
DROP TABLE IF EXISTS ticket CASCADE;
CREATE TABLE ticket (
    ticketID SERIAL PRIMARY KEY, --the unique ID for the ticket type, used for the primary key
    gigID INTEGER NOT NULL, --the ID of the gig which the ticket is for, used to link to the gig_ticket table
    pricetype VARCHAR(2) NOT NULL, --the type of ticket for the specific gig
    cost INTEGER CHECK (cost >= 0) NOT NULL, --the cost of the ticket
    customername VARCHAR(100) NOT NULL, --the name of the customer who bought the ticket
    customeremail VARCHAR(100) NOT NULL, --the email of the customer who bought the ticket

    FOREIGN KEY (gigID, pricetype) REFERENCES gig_ticket (gigID, pricetype) --references the gig_ticket table
    --(note that the ticket isn't deleted if the gig_ticket is deleted)
);








--VIEWS
/*
This creates a view that can be used to show the lineup of a gig
The gig and act_gig tables are joined by the gigID. Then this table is joined to the act table by the actID. The resulting join can therefore contain all the information necessary for the lineup.
The gigID, actID, duration and ontime are all projected out of the join. Using the ontime and duration the end time of a specific act is calculated.
The acts are ordered by their gigID, then ordered by the ontime.

This view can then be used to select the lineup of a specific gig, with extra useful information.
*/

DROP VIEW IF EXISTS gigLineup;
CREATE OR REPLACE VIEW gigLineup AS --defines the view as gigLineup
SELECT  
    row_number() OVER (PARTITION BY g.gigID ORDER BY ag.ontime) AS viewOrderingNum, --adds a row number which dynamically changes based on the ordering condition, the row number resets for each different gigID
    g.gigID, --projects out the gigID from the gig table
    a.actID, --projects out the actID from the act table
    a.actname, --prokects out the name of the act from act table
    a.genre, --projects out the genre of the act
    ag.duration, --projects out the duration of the act from the act table
    ag.ontime AS actstart, --projects out the ontime of the act as the act start
    ag.ontime + INTERVAL '1 minute' * ag.duration AS actend, --performs a calculation to find the end time of the act

    --to get the time in minutes until the next act at the gig
    COALESCE( --returns the first non null value out of the options. AKA if there is no act after the current act at the gig, then we return -1 instead
        CAST( --turns the result of the query into the int type (uses AS INT later on)
            EXTRACT( EPOCH FROM ( --we want to extract the epoch out of the time we're about to specify (epoch is a second count from the 1st of January 1970)
                    LEAD(ag.ontime) --the ag.ontime from the next act
                    OVER (PARTITION BY g.gigID ORDER BY ag.ontime) --the next act is defined by the one in the same gigID after the acts are ordered by the ontime
                    - (ag.ontime + INTERVAL '1 minute' * ag.duration) --takes away the finish time of the current act
                )
            ) / 60 --divides the result from epoch by 60 to get it in minutes
        AS INT)
    , -1) --or coalesce selects -1
    AS intervaltonext --stores the result in the column intervaltonext

--joins together tables for the projections
FROM gig g
JOIN act_gig ag ON g.gigID = ag.gigID
JOIN act a ON ag.actID = a.actID
WHERE gigstatus <> 'C' --does not include gigs that have been cancelled
ORDER BY g.gigID, viewOrderingNum; --the assigned row numbers based on an ordering over gigID and ontime


/*
This creates a view which can be used to show the lineup of an act
The actID, gigID and ontime (as actstart) are projected out of the act_gig table. The actend can then be calculated from the ontime.
The gigs are ordered by the actID, then ordered by the ontime.
The interval between two performances for the same act is calculated and stored.
The true/false state of whether a performance overlaps with a different performance for the same act is also calculated and stored
*/
DROP VIEW IF EXISTS actLineup;
CREATE OR REPLACE VIEW actLineup AS --defines the view as act_lineup
SELECT
    row_number() OVER (PARTITION BY actID ORDER BY ontime) AS viewOrderingNum, --adds a row number which dynamically changes based on the ordering condition, the row number resets for each different actID
    ag.actID,--projects out the actID
    ag.gigID, --projects out the gigID
    ag.duration, --projects out the duration of the act
    ag.ontime AS actstart, --projects out the ontime of the act as actstart
    ag.ontime + INTERVAL '1 minute' * ag.duration AS actend, --calculates the end of the act 

    --to get the time in minutes until the next time the act performs
    COALESCE( --returns the first non null value out of the options. AKA if there is no performance for the act after the current performance, then we return -1 instead
        CAST( --turns the result of the query into the int type (uses AS INT later on)
            EXTRACT( EPOCH FROM ( --we want to extract the epoch out of the time we're about to specify (epoch is a second count from the 1st of January 1970)
                    LEAD(ontime) --the ontime from the next performance
                    OVER (PARTITION BY actID ORDER BY ontime) --the next performance is defined by the one in the same actID after this performance is ordered by the ontime
                    - (ontime + INTERVAL '1 minute' * duration) --takes away the finish time of the current gig performance
                )
            ) / 60 --divides the result from epoch by 60 to get it in minutes
        AS INT)
    , -1) --or coalesce selects -1
    AS intervaltonext --stores the result in the column intervaltonext

FROM act_gig ag --taken from the act_gig table
JOIN gig g ON g.gigID = ag.gigID
WHERE g.gigstatus <> 'C' --does not include performances where the gig has been cancelled
ORDER BY ag.actID, viewOrderingNum; --order the results by actID and then the ontime


/*
This creates a view which can be used to show the lineup of a specific venue
The gig table is joined to the act gig table by the gigID. This join can then be used to get all of the other results
The venueID, gigID and gigdatetime are projected out of the gig table.
The ontime and duration are used from the act_gig table to calculate the maximum ontime for the gig in question, this is then used to calculate the final finish time of the gig
The gigs are ordered by the venueID, then ordered by the gigdatetime.
*/
DROP VIEW IF EXISTS venueLineup;
CREATE OR REPLACE VIEW venueLineup AS
SELECT
    row_number() OVER (PARTITION BY g.venueID ORDER BY g.gigdatetime) AS viewOrderingNum, --adds a row number which dynamically changes based on the ordering condition, the row number resets for each different venueID
    g.venueID, --projects out the venueID
    g.gigID, --projects out the gigID
    g.gigdatetime AS gigstart, --projects out the gigdatetime as gigstart
    MAX(ag.ontime + INTERVAL '1 minute' * ag.duration) AS gigend, --finds the maximum end time for a specific gig
    (MAX(ag.ontime + INTERVAL '1 minute' * ag.duration) - g.gigdatetime) AS giglength, --finds the length of the gig

    --to get the time in minutes until the next gig at the venue
    COALESCE( --returns the first non null value out of the options. AKA if there is no performance for the venue after the current gig, then we return -1 instead
        CAST( --turns the result of the query into the int type (uses AS INT later on)
            EXTRACT( EPOCH FROM ( --we want to extract the epoch out of the time we're about to specify (epoch is a second count from the 1st of January 1970)
                    LEAD(g.gigdatetime) --the start time for the next gig
                    OVER (PARTITION BY g.venueID ORDER BY g.gigdatetime) --the next performance is defined by the one in the same venueID after this venue is ordered by the gigdatetime
                    - (MAX(ag.ontime + INTERVAL '1 minute' * ag.duration)) --takes away the finish time of the current gig 
                )
            ) / 60 --divides the result from epoch by 60 to get it in minutes
        AS INT)
    , -1) --or coalesce selects -1
    AS intervaltonext --stores the result in the column intervaltonext

FROM gig g
JOIN act_gig ag ON g.gigID = ag.gigID --joins gig and act_gig on the 
WHERE g.gigstatus <> 'C' --does not include performances where the gig has been cancelled
GROUP BY g.venueID, g.gigID, g.gigdatetime --group by necessary columns
ORDER BY g.venueID, viewOrderingNum; --orders the results by venueID then gigdatetime 
