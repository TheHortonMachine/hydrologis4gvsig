cp gvsig-devel.properties ~/.gvsig-devel.properties
mvn install -Dmaven.test.skip=true  -Dmaven.javadoc.skip=true -Danimal.sniffer.skip=true -Dsource.skip=true
