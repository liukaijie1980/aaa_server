#!/bin/bash
QuerySQL="select radacctid, concat(username,realm) as user, acctsessionid, framedipaddress,nasipaddress from online_user where kickme!=0"


current_dir=$(dirname $(readlink -f "$0"))
RadiusClientPath=${current_dir}/radclient
host=mysql
user="service_user"
password="123456"
database="radius"

#BRAS setting: radiusserver authorization 10,4.2,241 destination-port 3799 shared-key Radius@3799
#secret="Radius@3799"
#secret="Huawei@123"

#echo ${current_dir}
#echo ${RadiusClientPath}

while true
do
  mysql -h $host --user=$user --password=$password --database=$database --execute="$QuerySQL" -s -N > ${current_dir}/sqlresult.coa

  while read line
  do
     UserName=`echo $line|awk '{print $2}'` 
     acctsessionid=`echo $line|awk '{print $3}'` 
     framedipaddress=`echo $line|awk '{print $4}'` 
     nasipaddress=`echo $line|awk '{print $5}'` 

     echo "User-Name = "$UserName  > ${current_dir}/temp.coa
     echo "Framed-IP-Address = "$framedipaddress >> ${current_dir}/temp.coa
     echo "Acct-Session-Id = "$acctsessionid  >> ${current_dir}/temp.coa
     secret=$(mysql -h $host --user=$user --password=$password --database=$database --execute="SELECT  get_coa_secret( '${nasipaddress}') " -s -N)   
     cat ${current_dir}/temp.coa | ${RadiusClientPath} ${nasipaddress}:3799 disconnect $secret -x -r 1  2> ${current_dir}/err.coa  

 
  done < ${current_dir}/sqlresult.coa
  sleep 0.5
done 
