-- test
INSERT INTO insurance_customer_info (cust_id,name,staff_id,policy_number)  
VALUES(15905,'俞臃',33,3);

INSERT INTO insurance_policy (p_id,project_name,p_no,p_type,insured_amount,available_amount,cust_id)  
VALUES(15906,'乐享人生','1','P001',11100,11100,15905),
(15908,'乐享人生','2','P002',7900,7900,15905),
(15910,'乐享人生','3','P002',34300,34300,15905);

INSERT INTO insurance_claims (c_id,p_id,claims_approve_result,claims_approve_amount,claims_reject_reason)  
VALUES(15907,15906,'reject',0,'单据不完整'),
(15909,15908,'approve',2400,null),
(15911,15910,'approve',33200,null);

UPDATE insurance_policy SET available_amount=5500 WHERE p_id=15908; 

UPDATE insurance_policy SET available_amount=1100 WHERE p_id=15910; 

