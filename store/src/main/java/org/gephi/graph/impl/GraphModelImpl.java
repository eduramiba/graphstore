/*
 * Copyright 2012-2013 Gephi Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gephi.graph.impl;

import org.gephi.graph.api.Configuration;
import org.gephi.graph.api.Index;
import org.gephi.graph.api.Table;
import org.gephi.graph.api.TimeFormat;
import org.gephi.graph.api.Interval;
import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.DirectedSubgraph;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphBridge;
import org.gephi.graph.api.GraphFactory;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Origin;
import org.gephi.graph.api.Subgraph;
import org.gephi.graph.api.UndirectedGraph;
import org.gephi.graph.api.UndirectedSubgraph;
import org.gephi.graph.api.TimeIndex;
import org.joda.time.DateTimeZone;
import org.gephi.graph.api.TimeRepresentation;
import org.gephi.graph.api.types.IntervalDoubleMap;
import org.gephi.graph.api.types.IntervalSet;
import org.gephi.graph.api.types.TimestampDoubleMap;
import org.gephi.graph.api.types.TimestampSet;

public class GraphModelImpl implements GraphModel {

    protected final Configuration configuration;
    protected final GraphStore store;
    protected final GraphBridgeImpl graphBridge;

    public GraphModelImpl() {
        this(new Configuration());
    }

    public GraphModelImpl(Configuration config) {
        configuration = config.copy();
        store = new GraphStore(this);
        graphBridge = new GraphBridgeImpl(store);
    }

    @Override
    public GraphFactory factory() {
        return store.factory;
    }

    @Override
    public GraphBridge bridge() {
        return graphBridge;
    }

    @Override
    public Graph getGraph() {
        return store;
    }

    @Override
    public Graph getGraphVisible() {
        return getGraph(store.viewStore.visibleView);
    }

    @Override
    public Subgraph getGraph(GraphView view) {
        if (store.isUndirected()) {
            return store.viewStore.getUndirectedGraph(view);
        }
        return store.viewStore.getDirectedGraph(view);
    }

    @Override
    public DirectedGraph getDirectedGraph() {
        return store;
    }

    @Override
    public DirectedGraph getDirectedGraphVisible() {
        return getDirectedGraph(store.viewStore.visibleView);
    }

    @Override
    public UndirectedGraph getUndirectedGraph() {
        return store.undirectedDecorator;
    }

    @Override
    public UndirectedGraph getUndirectedGraphVisible() {
        return getUndirectedGraph(store.viewStore.visibleView);
    }

    @Override
    public DirectedSubgraph getDirectedGraph(GraphView view) {
        return store.viewStore.getDirectedGraph(view);
    }

    @Override
    public UndirectedSubgraph getUndirectedGraph(GraphView view) {
        return store.viewStore.getUndirectedGraph(view);
    }

    @Override
    public GraphView getVisibleView() {
        return store.viewStore.getVisibleView();
    }

    @Override
    public void setVisibleView(GraphView view) {
        store.autoWriteLock();
        try {
            store.viewStore.setVisibleView(view);
        } finally {
            store.autoWriteUnlock();
        }
    }

    @Override
    public int addEdgeType(Object label) {
        store.autoWriteLock();
        try {
            return store.edgeTypeStore.addType(label);
        } finally {
            store.autoWriteUnlock();
        }
    }

    @Override
    public int getEdgeType(Object label) {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.getId(label);
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Object getEdgeLabel(int id) {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.getLabel(id);
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public boolean isMultiGraph() {
        store.autoReadLock();
        try {
            return store.edgeTypeStore.size() > 1;
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public boolean isDynamic() {
        store.autoReadLock();
        try {
            return !store.timeStore.isEmpty();
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public boolean isDirected() {
        return store.isDirected();
    }

    @Override
    public boolean isUndirected() {
        return store.isUndirected();
    }

    @Override
    public boolean isMixed() {
        return store.isMixed();
    }

    @Override
    public GraphView createView() {
        return store.viewStore.createView();
    }

    @Override
    public GraphView createView(boolean node, boolean edge) {
        return store.viewStore.createView(node, edge);
    }

    @Override
    public GraphView copyView(GraphView view) {
        return store.viewStore.createView(view);
    }

    @Override
    public GraphView copyView(GraphView view, boolean node, boolean edge) {
        return store.viewStore.createView(view, node, edge);
    }

    @Override
    public void destroyView(GraphView view) {
        store.viewStore.destroyView(view);
    }

    @Override
    public void setTimeInterval(GraphView view, Interval interval) {
        store.viewStore.setTimeInterval(view, interval);
    }

    @Override
    public Table getNodeTable() {
        return store.nodeTable;
    }

    @Override
    public Table getEdgeTable() {
        return store.edgeTable;
    }

    @Override
    public Index getNodeIndex() {
        return getNodeIndex(store.mainGraphView);
    }

    @Override
    public Index getNodeIndex(GraphView view) {
        IndexStore<Node> indexStore = store.nodeTable.store.indexStore;
        if (indexStore != null) {
            if (view.isMainView()) {
                return indexStore.getIndex(store);
            }
            return indexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public Index getEdgeIndex() {
        return getEdgeIndex(store.mainGraphView);
    }

    @Override
    public Index getEdgeIndex(GraphView view) {
        IndexStore<Edge> indexStore = store.edgeTable.store.indexStore;
        if (indexStore != null) {
            if (view.isMainView()) {
                return indexStore.getIndex(store);
            }
            return indexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public TimeIndex<Node> getNodeTimeIndex() {
        return getNodeTimeIndex(store.mainGraphView);
    }

    @Override
    public TimeIndex<Node> getNodeTimeIndex(GraphView view) {
        TimeIndexStore timeIndexStore = store.timeStore.nodeIndexStore;
        if (timeIndexStore != null) {
            if (view.isMainView()) {
                return timeIndexStore.getIndex(store);
            }
            return timeIndexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public TimeIndex<Edge> getEdgeTimeIndex() {
        return getEdgeTimeIndex(store.mainGraphView);
    }

    @Override
    public TimeIndex<Edge> getEdgeTimeIndex(GraphView view) {
        TimeIndexStore timeIndexStore = store.timeStore.edgeIndexStore;
        if (timeIndexStore != null) {
            if (view.isMainView()) {
                return timeIndexStore.getIndex(store);
            }
            return timeIndexStore.getIndex(((GraphViewImpl) view).directedDecorator);
        }
        return null;
    }

    @Override
    public GraphObserver createGraphObserver(Graph graph, boolean withGraphDiff) {
        store.autoWriteLock();
        try {
            if (graph.getView().isMainView()) {
                return store.createGraphObserver(graph, withGraphDiff);
            } else {
                return store.viewStore.createGraphObserver(graph, withGraphDiff);
            }
        } finally {
            store.autoWriteUnlock();
        }
    }

    @Override
    public TimeFormat getTimeFormat() {
        return store.timeFormat;
    }

    @Override
    public void setTimeFormat(TimeFormat timeFormat) {
        store.timeFormat = timeFormat;
    }

    @Override
    public DateTimeZone getTimeZone() {
        return store.timeZone;
    }

    @Override
    public void setTimeZone(DateTimeZone timeZone) {
        store.timeZone = timeZone;
    }

    @Override
    public Interval getTimeBounds() {
        return getTimeBounds(store.getView());
    }

    @Override
    public Interval getTimeBoundsVisible() {
        return getTimeBounds(getGraphVisible().getView());
    }

    @Override
    public Interval getTimeBounds(GraphView view) {
        TimeStore timeStore = store.timeStore;
        store.autoReadLock();
        try {
            double min = timeStore.getMin(getGraph(view));
            double max = timeStore.getMax(getGraph(view));
            return new Interval(min, max);
        } finally {
            store.autoReadUnlock();
        }
    }

    @Override
    public Configuration getConfiguration() {
        return configuration.copy();
    }

    @Override
    public void setConfiguration(Configuration config) {
        store.autoWriteLock();
        try {
            if (store.getNodeCount() > 0 || !store.attributes.isEmpty() || store.nodeTable.countColumns()
                    != GraphStoreConfiguration.NODE_DEFAULT_COLUMNS || store.edgeTable.countColumns() != GraphStoreConfiguration.EDGE_DEFAULT_COLUMNS
                    || store.edgeTypeStore.size() > 1) {
                throw new IllegalStateException("The store should be empty when modifying the configuration");
            }
            if (!config.getNodeIdType().equals(configuration.getNodeIdType())) {
                TableImpl<Node> nodeTable = store.nodeTable;
                nodeTable.store.removeColumn("id");
                nodeTable.store.addColumn(new ColumnImpl(nodeTable, "id", config.getNodeIdType(), "Id", null, Origin.PROPERTY, false, true));
                configuration.setNodeIdType(config.getNodeIdType());
            }
            if (!config.getEdgeIdType().equals(configuration.getEdgeIdType())) {
                TableImpl<Edge> edgeTable = store.edgeTable;
                edgeTable.store.removeColumn("id");
                edgeTable.store.addColumn(new ColumnImpl(edgeTable, "id", config.getEdgeIdType(), "Id", null, Origin.PROPERTY, false, true));
                configuration.setEdgeIdType(config.getEdgeIdType());
            }
            if (!config.getEdgeLabelType().equals(configuration.getEdgeLabelType())) {
                configuration.setEdgeLabelType(config.getEdgeLabelType());
            }
            if (!config.getTimeRepresentation().equals(configuration.getTimeRepresentation())) {
                TableImpl<Node> nodeTable = store.nodeTable;
                nodeTable.removeColumn(GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID);
                TableImpl<Edge> edgeTable = store.edgeTable;
                edgeTable.removeColumn(GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID);
                edgeTable.removeColumn(GraphStoreConfiguration.EDGE_WEIGHT_COLUMN_ID);
                if (config.getTimeRepresentation().equals(TimeRepresentation.TIMESTAMP)) {
                    nodeTable.store.addColumn(new ColumnImpl(nodeTable, GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID, TimestampSet.class, "Timestamp", null, Origin.PROPERTY, false, false));
                    edgeTable.store.addColumn(new ColumnImpl(nodeTable, GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID, TimestampSet.class, "Timestamp", null, Origin.PROPERTY, false, false));
                    edgeTable.store.addColumn(new ColumnImpl(edgeTable, GraphStoreConfiguration.EDGE_WEIGHT_COLUMN_ID, TimestampDoubleMap.class, "Weight", null, Origin.PROPERTY, false, false));
                } else {
                    nodeTable.store.addColumn(new ColumnImpl(nodeTable, GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID, IntervalSet.class, "Interval", null, Origin.PROPERTY, false, false));
                    edgeTable.store.addColumn(new ColumnImpl(nodeTable, GraphStoreConfiguration.ELEMENT_TIMESET_COLUMN_ID, IntervalSet.class, "Interval", null, Origin.PROPERTY, false, false));
                    edgeTable.store.addColumn(new ColumnImpl(edgeTable, GraphStoreConfiguration.EDGE_WEIGHT_COLUMN_ID, IntervalDoubleMap.class, "Weight", null, Origin.PROPERTY, false, false));
                }
                configuration.setTimeRepresentation(config.getTimeRepresentation());
                store.timeStore.resetConfiguration();
            }
            store.factory.resetConfiguration();
        } finally {
            store.autoWriteUnlock();
        }
    }

    public void destroyGraphObserver(GraphObserver observer) {
        checkGraphObserver(observer);

        store.autoWriteLock();
        try {
            if (observer.getGraph().getView().isMainView()) {
                store.destroyGraphObserver((GraphObserverImpl) observer);
            } else {
                store.viewStore.destroyGraphObserver((GraphObserverImpl) observer);
            }
        } finally {
            store.autoWriteUnlock();
        }
    }

    public GraphStore getStore() {
        return store;
    }

    public boolean deepEquals(GraphModelImpl obj) {
        if (obj == null) {
            return false;
        }
        if (this.store != obj.store && (this.store == null || !this.store.deepEquals(obj.store))) {
            return false;
        }
        if (this.configuration != obj.configuration && (this.configuration == null || !this.configuration.equals(obj.configuration))) {
            return false;
        }
        return true;
    }

    private void checkGraphObserver(GraphObserver observer) {
        if (observer == null) {
            throw new NullPointerException();
        }
        if (!(observer instanceof GraphObserverImpl)) {
            throw new ClassCastException("The observer should be a GraphObserverImpl instance");
        }
        if (((GraphObserverImpl) observer).graphStore != store) {
            throw new RuntimeException("The observer doesn't belong to this store");
        }
    }
}
