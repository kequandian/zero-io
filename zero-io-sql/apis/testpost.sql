INSERT INTO t_devops_test(name) VALUES(#{addName});
delete from t_devops_test where t_devops_test.name=#{deleteName};
