cp gvsig-devel.properties ~/.gvsig-devel.properties
mvn install -Dmaven.test.skip=true  -Dmaven.javadoc.skip=true -Danimal.sniffer.skip=true -Dsource.skip=true

cp ~/.m2/repository/org/slf4j/slf4j-api/1.7.5/slf4j-api-1.7.5.jar ~/SOFTWARE/GVSIG/LATEST/gvSIG/extensiones/org.jgrasstools.gvsig.base/lib/
