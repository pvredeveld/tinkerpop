/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.structure.util.star;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.apache.tinkerpop.gremlin.structure.io.graphson.GraphSONTokens;
import org.apache.tinkerpop.gremlin.structure.util.Attachable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class StarGraphGraphSONSerializer extends StdSerializer<StarGraphGraphSONSerializer.DirectionalStarGraph> {
    public StarGraphGraphSONSerializer() {
        super(DirectionalStarGraph.class);
    }

    @Override
    public void serialize(final DirectionalStarGraph starGraph, final JsonGenerator jsonGenerator,
                          final SerializerProvider serializerProvider) throws IOException, JsonGenerationException {
        ser(starGraph, jsonGenerator, serializerProvider, null);
    }

    @Override
    public void serializeWithType(final DirectionalStarGraph starGraph, final JsonGenerator jsonGenerator,
                                  final SerializerProvider serializerProvider,
                                  final TypeSerializer typeSerializer) throws IOException, JsonProcessingException {
        ser(starGraph, jsonGenerator, serializerProvider, typeSerializer);
    }

    private void ser(final DirectionalStarGraph directionalStarGraph, final JsonGenerator jsonGenerator,
                     final SerializerProvider serializerProvider,
                     final TypeSerializer typeSerializer) throws IOException, JsonProcessingException {
        final StarGraph starGraph = directionalStarGraph.getStarGraphToSerialize();
        jsonGenerator.writeStartObject();
        if (typeSerializer != null) jsonGenerator.writeStringField(GraphSONTokens.CLASS, HashMap.class.getName());
        writeWithType(GraphSONTokens.ID, starGraph.starVertex.id, jsonGenerator, serializerProvider, typeSerializer);
        jsonGenerator.writeStringField(GraphSONTokens.LABEL, starGraph.starVertex.label);
        if (directionalStarGraph.direction != null) writeEdges(directionalStarGraph, jsonGenerator, serializerProvider, typeSerializer, Direction.IN);
        if (directionalStarGraph.direction != null) writeEdges(directionalStarGraph, jsonGenerator, serializerProvider, typeSerializer, Direction.OUT);
        if (starGraph.starVertex.vertexProperties != null && !starGraph.starVertex.vertexProperties.isEmpty()) {
            jsonGenerator.writeObjectFieldStart(GraphSONTokens.PROPERTIES);
            if (typeSerializer != null) jsonGenerator.writeStringField(GraphSONTokens.CLASS, HashMap.class.getName());
            for (final Map.Entry<String, List<VertexProperty>> vp : starGraph.starVertex.vertexProperties.entrySet()) {
                jsonGenerator.writeArrayFieldStart(vp.getKey());
                if (typeSerializer != null) {
                    jsonGenerator.writeString(ArrayList.class.getName());
                    jsonGenerator.writeStartArray();
                }
                for (final VertexProperty property : vp.getValue()) {
                    jsonGenerator.writeStartObject();
                    if (typeSerializer != null) jsonGenerator.writeStringField(GraphSONTokens.CLASS, HashMap.class.getName());
                    writeWithType(GraphSONTokens.ID, property.id(), jsonGenerator, serializerProvider, typeSerializer);
                    writeWithType(GraphSONTokens.VALUE, property.value(), jsonGenerator, serializerProvider, typeSerializer);
                    final Iterator<Property<Object>> metaProperties = property.properties();
                    if (metaProperties.hasNext()) {
                        jsonGenerator.writeObjectFieldStart(GraphSONTokens.PROPERTIES);
                        if (typeSerializer != null) jsonGenerator.writeStringField(GraphSONTokens.CLASS, HashMap.class.getName());
                        while (metaProperties.hasNext()) {
                            final Property<Object> meta = metaProperties.next();
                            writeWithType(meta.key(), meta.value(), jsonGenerator, serializerProvider, typeSerializer);
                        }
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
                if (typeSerializer != null) jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndObject();
        }
    }

    private void writeEdges(final DirectionalStarGraph directionalStarGraph, final JsonGenerator jsonGenerator,
                            final SerializerProvider serializerProvider,
                            final TypeSerializer typeSerializer,
                            final Direction direction)  throws IOException, JsonProcessingException {
        // only write edges if there are some AND if the user requested them to be serialized AND if they match
        // the direction being serialized by the format
        final StarGraph starGraph = directionalStarGraph.getStarGraphToSerialize();
        final Direction edgeDirectionToSerialize = directionalStarGraph.getDirection();
        final Map<String, List<Edge>> starEdges = direction.equals(Direction.OUT) ? starGraph.starVertex.outEdges : starGraph.starVertex.inEdges;
        final boolean writeEdges = null != starEdges && edgeDirectionToSerialize != null
                && (edgeDirectionToSerialize == direction || edgeDirectionToSerialize == Direction.BOTH);
        if (writeEdges) {
            jsonGenerator.writeObjectFieldStart(direction == Direction.IN ? GraphSONTokens.IN_E : GraphSONTokens.OUT_E);
            if (typeSerializer != null) jsonGenerator.writeStringField(GraphSONTokens.CLASS, HashMap.class.getName());
            for (final Map.Entry<String, List<Edge>> edges : starEdges.entrySet()) {
                jsonGenerator.writeArrayFieldStart(edges.getKey());
                if (typeSerializer != null) {
                    jsonGenerator.writeString(ArrayList.class.getName());
                    jsonGenerator.writeStartArray();
                }
                for (final Edge edge : edges.getValue()) {
                    jsonGenerator.writeStartObject();
                    if (typeSerializer != null) jsonGenerator.writeStringField(GraphSONTokens.CLASS, HashMap.class.getName());
                    writeWithType(GraphSONTokens.ID, edge.id(), jsonGenerator, serializerProvider, typeSerializer);
                    writeWithType(direction.equals(Direction.OUT) ? GraphSONTokens.IN : GraphSONTokens.OUT,
                            direction.equals(Direction.OUT) ? edge.inVertex().id() : edge.outVertex().id(),
                            jsonGenerator, serializerProvider, typeSerializer);
                    final Iterator<Property<Object>> metaProperties = edge.properties();
                    if (metaProperties.hasNext()) {
                        jsonGenerator.writeObjectFieldStart(GraphSONTokens.PROPERTIES);
                        if (typeSerializer != null) jsonGenerator.writeStringField(GraphSONTokens.CLASS, HashMap.class.getName());
                        while (metaProperties.hasNext()) {
                            final Property<Object> meta = metaProperties.next();
                            writeWithType(meta.key(), meta.value(), jsonGenerator, serializerProvider, typeSerializer);
                        }
                        jsonGenerator.writeEndObject();
                    }
                    jsonGenerator.writeEndObject();
                }
                jsonGenerator.writeEndArray();
                if (typeSerializer != null) jsonGenerator.writeEndArray();
            }
            jsonGenerator.writeEndObject();
        }
    }

    private static void writeWithType(final String key, final Object object, final JsonGenerator jsonGenerator,
                                      final SerializerProvider serializerProvider,
                                      final TypeSerializer typeSerializer) throws IOException {
        final JsonSerializer<Object> serializer = serializerProvider.findValueSerializer(object.getClass(), null);
        if (typeSerializer != null) {
            jsonGenerator.writeFieldName(key);
            serializer.serializeWithType(object, jsonGenerator, serializerProvider, typeSerializer);
        } else {
            jsonGenerator.writeObjectField(key, object);
        }
    }

    /**
     * A helper function for reading vertex edges from a serialized {@link StarGraph} (i.e. a {@link Map}) generated by
     * {@link StarGraphGraphSONSerializer}.
     */
    public static void readStarGraphEdges(final Function<Attachable<Edge>, Edge> edgeMaker,
                                          final StarGraph starGraph,
                                          final Map<String, Object> vertexData,
                                          final String direction) throws IOException {
        final Map<String, List<Map<String,Object>>> edgeDatas = (Map<String, List<Map<String,Object>>>) vertexData.get(direction);
        for (Map.Entry<String, List<Map<String,Object>>> edgeData : edgeDatas.entrySet()) {
            for (Map<String,Object> inner : edgeData.getValue()) {
                final StarGraph.StarEdge starEdge;
                if (direction.equals(GraphSONTokens.OUT_E))
                    starEdge = (StarGraph.StarEdge) starGraph.getStarVertex().addOutEdge(edgeData.getKey(), starGraph.addVertex(T.id, inner.get(GraphSONTokens.IN)), T.id, inner.get(GraphSONTokens.ID));
                else
                    starEdge = (StarGraph.StarEdge) starGraph.getStarVertex().addInEdge(edgeData.getKey(), starGraph.addVertex(T.id, inner.get(GraphSONTokens.OUT)), T.id, inner.get(GraphSONTokens.ID));

                if (inner.containsKey(GraphSONTokens.PROPERTIES)) {
                    final Map<String, Object> edgePropertyData = (Map<String, Object>) inner.get(GraphSONTokens.PROPERTIES);
                    for (Map.Entry<String, Object> epd : edgePropertyData.entrySet()) {
                        starEdge.property(epd.getKey(), epd.getValue());
                    }
                }

                if (edgeMaker != null) edgeMaker.apply(starEdge);
            }
        }
    }

    /**
     * A helper function for reading a serialized {@link StarGraph} from a {@link Map} generated by
     * {@link StarGraphGraphSONSerializer}.
     */
    public static StarGraph readStarGraphVertex(final Map<String, Object> vertexData) throws IOException {
        final StarGraph starGraph = StarGraph.open();
        starGraph.addVertex(T.id, vertexData.get(GraphSONTokens.ID), T.label, vertexData.get(GraphSONTokens.LABEL));
        if (vertexData.containsKey(GraphSONTokens.PROPERTIES)) {
            final Map<String, List<Map<String, Object>>> properties = (Map<String, List<Map<String, Object>>>) vertexData.get(GraphSONTokens.PROPERTIES);
            for (Map.Entry<String, List<Map<String, Object>>> property : properties.entrySet()) {
                for (Map<String, Object> p : property.getValue()) {
                    // todo: cardinality - same as gryo right now???
                    final StarGraph.StarVertexProperty vp = (StarGraph.StarVertexProperty) starGraph.getStarVertex().property(VertexProperty.Cardinality.list, property.getKey(), p.get(GraphSONTokens.VALUE), T.id, p.get(GraphSONTokens.ID));
                    if (p.containsKey(GraphSONTokens.PROPERTIES)) {
                        final Map<String, Object> edgePropertyData = (Map<String, Object>) p.get(GraphSONTokens.PROPERTIES);
                        for (Map.Entry<String, Object> epd : edgePropertyData.entrySet()) {
                            vp.property(epd.getKey(), epd.getValue());
                        }
                    }
                }
            }
        }

        return starGraph;
    }

    public static class DirectionalStarGraph {
        private final Direction direction;
        private final StarGraph starGraphToSerialize;

        public DirectionalStarGraph(final StarGraph starGraphToSerialize, final Direction direction) {
            this.direction = direction;
            this.starGraphToSerialize = starGraphToSerialize;
        }

        public Direction getDirection() {
            return direction;
        }

        public StarGraph getStarGraphToSerialize() {
            return starGraphToSerialize;
        }
    }
}
