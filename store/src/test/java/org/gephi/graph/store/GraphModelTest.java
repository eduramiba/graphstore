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
package org.gephi.graph.store;

import java.util.Arrays;
import org.gephi.attribute.api.Column;
import org.gephi.attribute.api.Index;
import org.gephi.attribute.api.Table;
import org.gephi.attribute.api.TimeFormat;
import org.gephi.attribute.api.TimestampIndex;
import org.gephi.attribute.time.Interval;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphObserver;
import org.gephi.graph.api.GraphView;
import org.gephi.graph.api.Node;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GraphModelTest {

    @Test
    public void testEmpty() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertNotNull(graphModel.getStore());
        Assert.assertNotNull(graphModel.getGraph());
        Assert.assertNotNull(graphModel.factory());
        Assert.assertNotNull(graphModel.getDirectedGraph());
        Assert.assertNotNull(graphModel.getUndirectedGraph());
        Assert.assertNotNull(graphModel.getNodeTable());
        Assert.assertNotNull(graphModel.getEdgeTable());
    }

    @Test
    public void testGetGraphVisibleDefault() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertSame(graphModel.getGraphVisible(), graphModel.getGraph());
    }

    @Test
    public void testGetGraphVisible() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        graphModel.setVisibleView(view);
        Assert.assertSame(graphModel.getGraphVisible(), graphModel.getGraph(view));
        graphModel.setVisibleView(null);
        Assert.assertSame(graphModel.getGraphVisible(), graphModel.getGraph());
        graphModel.setVisibleView(view);
        graphModel.setVisibleView(graphModel.store.getView());
        Assert.assertSame(graphModel.getGraphVisible(), graphModel.getGraph());
    }

    @Test
    public void testGetVisibleViewDefault() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertSame(graphModel.getVisibleView(), graphModel.store.mainGraphView);
    }

    @Test
    public void testGetVisibleView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        graphModel.setVisibleView(view);
        Assert.assertSame(graphModel.getVisibleView(), view);
        graphModel.setVisibleView(null);
        Assert.assertSame(graphModel.getVisibleView(), graphModel.store.getView());
    }

    @Test
    public void testAddEdgeType() {
        GraphModelImpl graphModel = new GraphModelImpl();
        int typeId = graphModel.addEdgeType("foo");
        Assert.assertEquals(graphModel.getEdgeLabel(typeId), "foo");
        Assert.assertEquals(graphModel.getEdgeType("foo"), typeId);
    }

    @Test
    public void testIsMultiGraph() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertFalse(graphModel.isMultiGraph());
        graphModel.addEdgeType("foo");
        Assert.assertTrue(graphModel.isMultiGraph());
    }

    @Test
    public void testIsDirectedDefault() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertTrue(graphModel.isDirected());
        Assert.assertFalse(graphModel.isUndirected());
    }

    @Test
    public void testIsUndirected() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[]{n1, n2}));
        Edge e = graphModel.factory().newEdge(n1, n2, false);
        graphModel.getStore().addEdge(e);
        Assert.assertTrue(graphModel.isUndirected());
        Assert.assertFalse(graphModel.isDirected());
    }

    @Test
    public void testIsMixed() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        Node n3 = graphModel.factory().newNode("3");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[]{n1, n2, n3}));
        Edge e1 = graphModel.factory().newEdge(n1, n2, false);
        Edge e2 = graphModel.factory().newEdge(n1, n3, true);
        graphModel.getStore().addAllEdges(Arrays.asList(new Edge[]{e1, e2}));
        Assert.assertTrue(graphModel.isMixed());
        Assert.assertFalse(graphModel.isDirected());
        Assert.assertFalse(graphModel.isUndirected());
    }

    @Test
    public void testCreateView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Assert.assertNotNull(view);
        Assert.assertSame(view.getGraphModel(), graphModel);
    }

    @Test
    public void testCreateViewCustom() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView(true, false);
        Assert.assertTrue(view.isNodeView());
        Assert.assertFalse(view.isEdgeView());
    }

    @Test
    public void testCopyView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        GraphView viewCopy = graphModel.copyView(view);
        Assert.assertNotNull(viewCopy);
        Assert.assertSame(view.getGraphModel(), graphModel);
    }

    @Test
    public void testCopyViewCustom() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        GraphView viewCopy = graphModel.copyView(view, true, false);
        Assert.assertTrue(viewCopy.isNodeView());
        Assert.assertFalse(viewCopy.isEdgeView());
    }

    @Test
    public void testDestroyView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        graphModel.destroyView(view);
        Assert.assertFalse(graphModel.store.viewStore.contains(view));
    }

    @Test
    public void testSetTimeInterval() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Interval i = new Interval(1.0, 2.0);
        graphModel.setTimeInterval(view, i);
        Assert.assertEquals(view.getTimeInterval(), i);
    }

    @Test
    public void testSetTimeFormat() {
        GraphModelImpl graphModel = new GraphModelImpl();
        graphModel.setTimeFormat(TimeFormat.DATETIME);
        Assert.assertEquals(graphModel.getTimeFormat(), TimeFormat.DATETIME);
        graphModel.setTimeFormat(TimeFormat.DOUBLE);
        Assert.assertEquals(graphModel.getTimeFormat(), TimeFormat.DOUBLE);
    }

    @Test
    public void testGetTimeBoundsMainView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertEquals(graphModel.getTimeBounds(), Interval.INFINITY_INTERVAL);
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        n.addTimestamp(1.0);
        n.addTimestamp(5.0);
        Assert.assertEquals(graphModel.getTimeBounds(), new Interval(1.0, 5.0));
    }

    @Test
    public void testIsDynamic() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Assert.assertFalse(graphModel.isDynamic());
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        n.addTimestamp(1.0);
        Assert.assertTrue(graphModel.isDynamic());
    }

    @Test
    public void testGetTimeBoundsInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Assert.assertEquals(graphModel.getTimeBounds(view), Interval.INFINITY_INTERVAL);

        Node n1 = graphModel.factory().newNode("1");
        Node n2 = graphModel.factory().newNode("2");
        graphModel.getStore().addAllNodes(Arrays.asList(new Node[]{n1, n2}));

        n1.addTimestamp(1.0);
        n1.addTimestamp(5.0);

        n2.addTimestamp(2.0);
        n2.addTimestamp(3.0);

        graphModel.getGraph(view).addNode(n2);

        Assert.assertEquals(graphModel.getTimeBounds(view), new Interval(2.0, 3.0));
    }

    @Test
    public void testCreateGraphObserver() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphObserver obs = graphModel.createGraphObserver(graphModel.getGraph(), true);
        Assert.assertNotNull(obs);
        Assert.assertSame(obs.getGraph(), graphModel.getGraph());
    }

    @Test
    public void testDestroyGraphObserver() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphObserver obs = graphModel.createGraphObserver(graphModel.getGraph(), true);
        graphModel.destroyGraphObserver(obs);
        Assert.assertTrue(obs.isDestroyed());
    }

    @Test
    public void testCreateGraphObserverInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        GraphObserver obs = graphModel.createGraphObserver(graphModel.getGraph(view), true);
        Assert.assertNotNull(obs);
        Assert.assertSame(obs.getGraph(), graphModel.getGraph(view));
    }

    @Test
    public void testDestroyGraphObserverInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        GraphObserver obs = graphModel.createGraphObserver(graphModel.getGraph(view), true);
        graphModel.destroyGraphObserver(obs);
        Assert.assertTrue(obs.isDestroyed());
    }

    @Test
    public void testGetNodeIndex() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getNodeTable();
        Column col = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col, "bar");
        graphModel.getStore().addNode(n1);

        Index index = graphModel.getNodeIndex();
        Assert.assertEquals(index.count(col, "bar"), 1);
    }

    @Test
    public void testGetNodeIndexInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Table table = graphModel.getNodeTable();
        Column col = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        n1.setAttribute(col, "bar");
        graphModel.getStore().addNode(n1);
        graphModel.getGraph(view).fill();

        Index index = graphModel.getNodeIndex(view);
        Assert.assertEquals(index.count(col, "bar"), 1);
    }

    @Test
    public void testGetEdgeIndex() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Table table = graphModel.getEdgeTable();
        Column col = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n1);
        Edge e = graphModel.factory().newEdge(n1, n1);
        e.setAttribute(col, "bar");
        graphModel.getStore().addEdge(e);

        Index index = graphModel.getEdgeIndex();
        Assert.assertEquals(index.count(col, "bar"), 1);
    }

    @Test
    public void testGetEdgeIndexInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Table table = graphModel.getEdgeTable();
        Column col = table.addColumn("foo", String.class);

        Node n1 = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n1);
        Edge e = graphModel.factory().newEdge(n1, n1);
        e.setAttribute(col, "bar");
        graphModel.getStore().addEdge(e);

        graphModel.getGraph(view).fill();

        Index index = graphModel.getEdgeIndex(view);
        Assert.assertEquals(index.count(col, "bar"), 1);
    }

    @Test
    public void testGetNodeTimestampIndex() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        n.addTimestamp(1.0);

        TimestampIndex<Node> index = graphModel.getNodeTimestampIndex();
        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 1.0);
    }

    @Test
    public void testGetEdgeTimestampIndex() {
        GraphModelImpl graphModel = new GraphModelImpl();
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        Edge e = graphModel.factory().newEdge(n, n);
        graphModel.getStore().addEdge(e);
        e.addTimestamp(1.0);

        TimestampIndex<Edge> index = graphModel.getEdgeTimestampIndex();
        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 1.0);
    }
    
    @Test
    public void testGetNodeTimestampIndexInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        n.addTimestamp(1.0);
        graphModel.getGraph(view).fill();

        TimestampIndex<Node> index = graphModel.getNodeTimestampIndex(view);
        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 1.0);
    }

    @Test
    public void testGetEdgeTimestampIndexInView() {
        GraphModelImpl graphModel = new GraphModelImpl();
        GraphView view = graphModel.createView();
        Node n = graphModel.factory().newNode("1");
        graphModel.getStore().addNode(n);
        Edge e = graphModel.factory().newEdge(n, n);
        graphModel.getStore().addEdge(e);
        e.addTimestamp(1.0);
        graphModel.getGraph(view).fill();

        TimestampIndex<Edge> index = graphModel.getEdgeTimestampIndex(view);
        Assert.assertEquals(index.getMinTimestamp(), 1.0);
        Assert.assertEquals(index.getMaxTimestamp(), 1.0);
    }
}
