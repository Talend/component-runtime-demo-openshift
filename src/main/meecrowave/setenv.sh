#! /bin/sh
export MEECROWAVE_OPTS="$MEECROWAVE_OPTS -Dtalend.component.server.maven.repository=$MEECROWAVE_BASE/.m2"
export MEECROWAVE_OPTS="$MEECROWAVE_OPTS -Dtalend.component.server.component.coordinates=org.talend:demo:1.0.0"
export MEECROWAVE_OPTS="$MEECROWAVE_OPTS -Dtalend.component.server.documentation.active=true"
