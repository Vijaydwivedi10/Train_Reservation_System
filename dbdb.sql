drop database db;

Create database db;
\c db;

CREATE TABLE Trains (train_id int, doj date, coach_no int, coach_type char(2),berth_no int, berth_type char(2),booked int,primary key(train_id,doj,coach_type,coach_no,berth_no));
CREATE TABLE passenger(name varchar(256),pnr bigint, primary key(name, pnr));
CREATE TABLE ticket (pnr bigint ,train_id int, doj date, coach_no int, coach_type char(2),berth_no int, berth_type char(2),primary key(pnr,coach_no,berth_no),FOREIGN key (train_id,doj,coach_type,coach_no,berth_no) REFERENCES trains(train_id,doj,coach_type,coach_no,berth_no));



Create procedure booking_ticket(pnr bigint,passenger_number int,p_name text[],train_no int, input_doj date,input_coach_type char(2),avail int, book int) 
LANGUAGE 'plpgsql'
AS 
$$

DECLARE 
    total_seats int;
    input_berth_no INT;
    input_coach_no INT;
    berth_type_f CHAR(2);

BEGIN
	FOR i IN 1..(passenger_number) LOOP

   book = book + 1;
   avail = avail - 1;

    IF input_coach_type like 'AC' THEN
        total_seats := 18;
    ELSE 
        total_seats := 24;
    END IF;
    

    -- berth_no & coach_no
    IF book % total_seats = 0 THEN
        input_coach_no := book/total_seats;
        input_berth_no := total_seats;
    ELSE
        input_coach_no := floor(book/total_seats) + 1;
        input_berth_no := book%total_seats;
    END IF;
	
    -- berth_type
    IF input_coach_type like 'ac' THEN
    	CASE input_berth_no % 6
            WHEN 1 THEN
               berth_type_f := 'LB';
            WHEN 2 THEN
               berth_type_f := 'LB';
            WHEN 3 THEN
               berth_type_f := 'UB';
            WHEN 4 THEN
               berth_type_f := 'UB';
            WHEN 5 THEN
               berth_type_f := 'SL';
            WHEN 0 THEN
               berth_type_f := 'SU';
		END CASE;
    ELSE
    	CASE input_berth_no % 8
            WHEN 1 THEN
               berth_type_f := 'LB';
            WHEN 2 THEN
               berth_type_f := 'MB';
            WHEN 3 THEN
               berth_type_f := 'UB';
            WHEN 4 THEN
               berth_type_f := 'LB';
            WHEN 5 THEN
               berth_type_f := 'MB';
            WHEN 6 THEN
               berth_type_f := 'UB';
            WHEN 7 THEN
               berth_type_f := 'SL';
            WHEN 0 THEN
               berth_type_f := 'SU';
		END CASE;
    END IF;

    -- add passenger
    INSERT INTO passenger(name, pnr)
    VALUES(p_name[i], pnr); 


--    RAISE NOTICE 'values : %, %, % % % % %', book, total_seats, train_no, input_doj, input_coach_type, input_coach_no, input_berth_no;   

    -- insert into tickets
    INSERT INTO ticket(pnr,train_id, doj, coach_no, coach_type,berth_no,berth_type)
    VALUES(pnr, train_no, input_doj, input_coach_no, input_coach_type, input_berth_no, berth_type_f);
    

    -- update in train
    UPDATE trains
    SET booked = 1
    WHERE trains.train_id = train_no AND trains.doj = input_doj AND trains.coach_type like input_coach_type AND trains.coach_no = input_coach_no AND trains.berth_no = input_berth_no;
   
    
    END LOOP;
END$$;








CREATE PROCEDURE insert_train_table(train_id int, doj date, num_ac int , num_sl int)
LANGUAGE 'plpgsql'
AS 
$$

DECLARE
   coach_tp varchar(2);
   berth_tp varchar(2);
   total_seats int;

BEGIN

   total_seats := 18;
   coach_tp := 'AC';

       FOR i IN 1..(num_ac) LOOP
         FOR j IN 1..(total_seats) LOOP
            CASE j % 6
            WHEN 1 THEN
               berth_tp := 'LB';
            WHEN 2 THEN
               berth_tp := 'LB';
            WHEN 3 THEN
               berth_tp := 'UB';
            WHEN 4 THEN
               berth_tp := 'UB';
            WHEN 5 THEN
               berth_tp := 'SL';
            WHEN 0 THEN
               berth_tp := 'SU';
		   END CASE;
            		   
         INSERT INTO trains(train_id, doj, coach_no, coach_type, berth_no, berth_type, booked) 
         VALUES(train_id, doj, i , coach_tp, j , berth_tp , 0);
         END LOOP;
      END LOOP;


      total_seats = 24;
      coach_tp := 'SL';

      FOR i IN 1..(num_sl) LOOP
         FOR j IN 1..(total_seats) LOOP
            CASE J % 8
            WHEN 1 THEN
               berth_tp := 'LB';
            WHEN 2 THEN
               berth_tp := 'MB';
            WHEN 3 THEN
               berth_tp := 'UB';
            WHEN 4 THEN
               berth_tp := 'LB';
            WHEN 5 THEN
               berth_tp := 'MB';
            WHEN 6 THEN
               berth_tp := 'UB';
            WHEN 7 THEN
               berth_tp := 'SL';
            WHEN 0 THEN
               berth_tp := 'SU';
		      END CASE;

             INSERT INTO trains(train_id, doj, coach_no, coach_type, berth_no, berth_type, booked) 
             VALUES(train_id, doj, i , coach_tp, j , berth_tp , 0);
         END LOOP;
      END LOOP;
      
    END
$$;




ALTER DATABASE db SET DEFAULT_TRANSACTION_ISOLATION TO SERIALIZABLE;










