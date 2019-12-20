--Creates two test cards
--insert into card (currency_id, user_id) values ('GBP','user1');
--insert into card (currency_id, user_id) values ('GBP','user2');

insert into card (currency_id, user_id) values (1,'user1');
insert into card (currency_id, user_id) values (1,'user2');

--Create two test transactions for the first card
--insert into transaction (global_id,type_id,amount,card_id,currency_id,description)
--values ('test123','C',10,1,'GBP','add funds');
--insert into transaction (global_id,type_id,amount,card_id,currency_id,description)
--values ('test2345','D',-10,1,'GBP','remove funds');

insert into transaction (global_id,type_id,amount,card_id,currency_id,description)
values ('test123','C',10,1,1,'add funds');
insert into transaction (global_id,type_id,amount,card_id,currency_id,description)
values ('test2345','D',-10,1,1,'remove funds');