DELIMITER $$

DROP PROCEDURE IF EXISTS `ormakeplus`.`sp_UpdateCounter`$$
CREATE DEFINER=`root`@`localhost` PROCEDURE  `ormakeplus`.`sp_UpdateCounter`(

IN IN_USER VARCHAR(24),

IN IN_DATE VARCHAR(10),

IN IN_AGG_ID VARCHAR(99),

IN IN_F_CODE VARCHAR(24),

IN RETAIL_PRICE double,

IN WHOLESALE_PRICE double,

IN REMOTE_POLO_PRICE double,

IN REMOTE_ROLO_PRICE double,

IN LOCAL_POLO_PRICE double,

IN LOCAL_ROLO_PRICE double,

IN LOCAL_TRANSIT_PRICE double,

IN REMOTE_TRANSIT_PRICE double

)
BEGIN

DECLARE rowExist int;

DECLARE currentBal,newBal double;

select count(*) from aggregated_cdr where AGGREGATION_ID=IN_AGG_ID and STATUS="U" into rowExist;

If rowExist=0 then

INSERT INTO aggregated_cdr (USER,DATE,RETAIL_PRICE,WHOLESALE_PRICE,REMOTE_POLO,REMOTE_ROLO,
    LOCAL_POLO,LOCAL_ROLO,LOCAL_TRANSIT,REMOTE_TRANSIT,DELTA_BAL,AGGREGATION_ID,FINANCIAL_CODE)
VALUES (IN_USER,IN_DATE,RETAIL_PRICE,WHOLESALE_PRICE,REMOTE_POLO_PRICE,REMOTE_ROLO_PRICE,
    LOCAL_POLO_PRICE,LOCAL_ROLO_PRICE,LOCAL_TRANSIT_PRICE
    ,REMOTE_TRANSIT_PRICE,RETAIL_PRICE,IN_AGG_ID,IN_F_CODE);

else

select RETAIL_PRICE from aggregated_cdr where AGGREGATION_ID=IN_AGG_ID and STATUS="U" into currentBal;

set newBal = currentBal + RETAIL_PRICE;

update aggregated_cdr set RETAIL_PRICE = newBal, DELTA_BAL=DELTA_BAL+RETAIL_PRICE where AGGREGATION_ID=IN_AGG_ID and STATUS="U";

end if;

END $$

DELIMITER ;