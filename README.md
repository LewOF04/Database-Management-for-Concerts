# Design Choices

The first most obvious difference that could be added is to introduce a actgigid into the act_gig table. Currently in order to uniquely identify a act_gig record, you must create a composite key of the actID, gigID and ontime.
Two act_gigs cannot be the same act, at the same gig and at the same time, logically, so this composite key ensures uniqueness. However it is complicated to process within statements since there are three factors that must be considered when we want to know which act_gig we're talking about.

It would be useful to create a separate genre table which stores all information about specific genres. This would make it easier to identify relations between different acts, based upon shared genre attributes.
Although this isn't much use directly in the way our database is formed, it would allow for further expansion of the system to involve filtering by genre or recommending acts based upon a customer's purchase history.

Furthermore, although there are many constraints which are required to be implemented it might've been useful to include additional constraints which can further ensure the reliability of database information. One of which is checking the capacity of a venue is greater than 0.

Finally, it would be useful to ensure that there is only a limited number of ticket types. This could be done through a ticket type tabe which has information about what having that ticket type might require. E.g. a handicap ticket might only be valid for venues where there is wheelchair access.
This would also add more consistency between tickets sold, which is easier for the customer.

# Task Implementations

Constraints to comply with business rules are performed with a mixture of checks on schema level as well as a variety of sql queries being performed via prepared statements in java.
The constraints which require simply checks on the type, form or uniqueness of an attribute are performed on schema level, using the CHECK and UNIQUE keywords as well as FOREIGN KEY and PRIMARY KEY constraints.
The constraints which require checking for the presence of a specfic records attribute values which cause issues with other records is more complicated and instead requires SQL queries.
Within the java file a set of methods are defined, which, when provided all necessary information, can check that there are no violating states within the database for each constraint.
This is very efficient over using triggers or java based checking, as SQL can optimise the queries to get the information as quick as possible and return whether or not there is a violation of the constraints, if there is we can then deal with these scenarios.
The functionality of each of these SQL checks is described by comments within the code which outline how each check is performed.

A key attribute of the solution is the use of views to display the lineup of a gig, the lineup for an act and then lineup for a venue. These views included necessary information from the database about each gig/venue/actas well as addtional computed values.
By using these views it made it very easy to reference what acts are supposed to be where and whether or not there are any clashes within gigs or venues.
These views could also be highly beneficial for other functions which may be introduced, such as printing a running order for a gig, informing acts of their schedule, or when displaying the gig on the website.
The inclusion in the views of the viewOrderingNum and intervaltonext attributes provide easy ways to make comparisons and checks between records without additional computation later on in the process.
The viewOrderingNum is an easy way to identify the chronological order of events within the lineups whereas intervaltonext makes it easy to identify the time information between acts/gigs as well as which act/gig is considered the headliner.

## Task 1
An sql query is used to select the required information from the database about the gig which has been provided.
It uses a simple projection from the already defined gigLineup which already collects together all the necesary information for each act, as well as, providing a quick and simple way of determining the order of the results.
Minor type adjustments are made within the query to get the the start and end of the act in the required format.

This is then converted into a string[][] using the providedmethods, this can then be returned. If there were any issues then an empty String[][] is returned instead.

## Task 2
Task 2 first importantly disables auto commits and makes a savepoint of the database before it commences. This helps to ensure database integrity since we know what the state was before our changes were made as well as stopping an commits occuring which were not intended.

In order to insert into gig we need the venueID, since we are only provided with the venuename, the venueID relating to that name must be projected from venue.

Then we can insert the new gig into the gig database using all of the provided information. The gigstatus is set to 'G' to indicate that this gig is going ahead initially.

We then retrieve the ID for the gig we've just inserted. This will be uniquely identified as the venueID, gigdatetime and gigstatus that have just been inserted.

Then we must create the insert statement to insert all of the acts. To do this the amount of (?,?,?,?,?) inserted is found by how many different acts need to be inserted for the gig.
This is concatenated to the query so that all of the inserts can be done within a singular query. The array of acts given is then iterated over to add each parameter into the query ready for insert.

Finally, the base adult gig_ticket is inserted into the database with all of the necessary information.

Once all of the inserts are made, the variety of checks are performed to ensure that the database is in a valid state, if not an exception is thrown.

If at any point in task 2 an exception occurs, it will print out the error and then roll back the database to the state before the task was called to ensure the database is intact.

## Task 3
Task 3 first importantly disables auto commits and makes a savepoint of the database before it commences. This helps to ensure database integrity since we know what the state was before our changes were made as well as stopping an commits occuring which were not intended.

We first query the database to check that there is such a gig with the matching information for the ticket we want to buy. If not, an exception is thrown.

Then we query the database to check that there is such a gig_ticket with the matching information for the ticket we want to buy. If not, an exception is thrown.

Then we get the capacity of the venue where the gig is taking place and the amount of tickets sold for this particular gig. If the amount of tickets sold is the same as the capacity of the venue, then we can't sell anymore tickets and so an exception is thrown.

If there are no errors after this point then we can finally insert the ticket into the ticket table for this customer, with all of the required information present.

If at any point in task 3 an exception occurs, it will print out the error and then roll back the database to the state before the task was called to ensure the database is intact.

## Task 4
Task 4 first importantly disables auto commits and makes a savepoint of the database before it commences. This helps to ensure database integrity since we know what the state was before our changes were made as well as stopping an commits occuring which were not intended.

In the gigLineup table, a intervaltonext=-1 indicates that this is the final act of the gig and hence the headline act. The we first query the database to check whether the act we're deleting is the headline act, if so then this gig must be cancelled to the cancelGig method is called to perform all of these operations.

Otherwise, we attempt to update the acts accordingly:
    1. The acts which have to be deleted from the database are retrieved into a CTE, along with all information required for computation.
    2. The acts which are not the ones being deleted, are then selected into a CTE with all the information required for computation.
    3. The total interval of acts within the acts to be deleted, where the act to be deleted occurs before the one affected, is added up for each act within the affected cte.
    4. That number is then removed from the ontime of each act, moving them according to the time of the cancelled acts.

Then using the same CTE for the acts which have to be deleted we can delete these acts from the act_gig table.

All checks which may have been violated by these changes are then checked again to ensure the database is valid, if not an excetion is thrown. This exception is then caught and allowed the delete act method to be triggered. The database is rolled back to the previous state.

Delete act method:
    - Updates the gigstatus in gig to 'C' for the gig that has been cancelled
    - Updates the cost in ticket to 0 for all of the tickets which have been purchased for the gig_ticket
    - Retrieves the distinct name and email of all the customers linked to the tickets for the gig.
    - Turns the customers as a string array

If in task 4 an exception occurs, outside of the checks, it will print out the error and then roll back the database to the state before the task was called to ensure the database is intact.

## Task 5
A CTE called gigInfo is used to gather all of the information about the gig:
    - Table 1 the cost of the gig is the combination of the sum of all of the distinct act_gig records for the gig, plus the hire cost of the venue where the gig is being performed.
    - Table 2 the sold value of tickets is the sum of all of the costs of the tickets in ticket for a gig.
    - The minimum ticket cost is the lowest price gig_ticket for the gig.

The required number of tickets is determined by either the rounding up of the total cost / minimum ticket price IF the gig has yet to sell any tickets, or it is the greatest out of the (total cost - the amount sold and 0) / the minimum ticket cost. (If the gig is already breaking even then total cost - amound sold will be negative, so we'd select 0 instead).

This number can then be returned along with corresponding gigID.

## Task 6
This problem is first broken down into 5 CTEs which retrieve all of the necessary information:
    - Table 1 of the headliner acts for each gig
    - Table 2 where for each gig the headline act is listed along with the number of tickets sold for the gig and the year that the gig took place. These results are then grouped by year, so that there is the headline act and the total number of tickets they sold in that year as a headliner
    - Table 3 of actname (the headliner for the gig), the year of the gig casted to a string (so that it can be ordered alongside 'Total' later on) and the number of tickets sold for the year
    - Table 4 of the total number of tickets sold for each different gig where an certain act is the headline
    - Table 5 which combines the table of the yearly sales of a headliner and the total sales of that headliner into one table

Using these CTEs the necessary information can be projected and the table ordered by the number of tickets sold (paritioned by the act name) and the string comparisons of the years sold.

## Task 7
This problem is first broken down into 4 CTEs:
    - The first is a table of customers, the headline act they attended and the year that they attended that headline act
    - Table 2 which stores every headline act and an array of the years that they have performed a headline act_gig
    - Table 3 which stores each customer, the headline act they attended and the array of years that they attended the headline act
    - Table 4 which contains all of the customers who have attended the headline act in the same years that they have performed as well as the amount of tickets that the customer has bought for that specific headliner.

Using these CTEs we can then combine all of the acts which have had a headline and pair them up with the customers which have attened all of the years that they have performed. If there are no customers which have met the condition then '[None]' is subsitituted instead.

## Task 8
This problem is broken down into 3 CTEs:
    - Table 1 gets the average ticket price of every ticket ever sold.
    - Table 2 gets every pair of venues and acts as well as the sum of the cost of the acts fee and the venues hire cost
    - Table 3 gets the venue and act, and the total number of tickets that would be required, sold at the average price, to cover the cost of the act and venue

Using these CTEs we can then select the venue, act and number of tickets where the number of tickets required does not exceed the capacity of the venue. This is then ordered based upon the venue name and the number of tickets required.
