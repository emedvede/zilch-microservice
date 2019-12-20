--Insert reference data
--insert into currency (id) values ('GBP') ON CONFLICT (id) DO NOTHING;
insert into currency (name) values ('GBP') ON CONFLICT (name) DO NOTHING;

insert into transaction_type (id,description) values ('D', 'Debit transaction') ON CONFLICT (id) DO NOTHING;
insert into transaction_type (id,description) values ('C', 'Credit transaction') ON CONFLICT (id) DO NOTHING;

