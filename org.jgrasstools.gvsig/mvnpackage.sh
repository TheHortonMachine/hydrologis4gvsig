cp gvsig-devel.properties ~/.gvsig-devel.properties
mvn package -Dmaven.test.skip=true -Dmaven.javadoc.skip=true -Danimal.sniffer.skip=true -Dsource.skip=true
