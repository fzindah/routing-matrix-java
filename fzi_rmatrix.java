/*
 * Programmer: Farha Zindah, COSC 439/522, W '23
 * 
 * Main class: fzi_rmatrix.java
 * Classes: Node, Graph, ParseInput
 * 
 * A java program that takes two optional command line arguments
 * for input and output file, reads a weighed graph representing 
 * the network, and outputs the corresponding routing matrix. 
 * The matrix is printed both to the console and to a specified
 * output file.
 * 
 * Ex:
 * java fzi_rmatrix -i fzi_rmatrixi.txt -o fzi_rmatrixo
 * 
*/

package proj4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Main class fzi_rmatrix
 * @author Farha Zindah
 */
public class fzi_rmatrix {

	private static File input = new File("fzi_input.txt");
	private static File output = new File("fzi_output.txt");

	public static void main(String[] args) {
		try {
			// check command line arguments
			checkCommandArgs(args);
			
			// read undirected weighted graph from file
			ParseInput fileInput = new ParseInput(input);
			Graph graph = fileInput.parseGraph();
			
			// print graph header
			printHeader(graph);
			
			// run djikstra on all nodes and output routing matrix
			runDjikstra(graph, fileInput);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Method that runs the Djistra algorithm on 
	 * each node in a graph representing a network
	 * @param graph
	 * @param fileInput
	 */
	public static void runDjikstra(Graph g, ParseInput fileInput) {
		
		// call djikstra's on all nodes
		for (int i = 0; i < g.getNodes().length; i++) {
			Graph graph = fileInput.parseGraph();
		
			Graph newGraph = djikstra(graph, graph.getNodes()[i]);
			newGraph.setSource(graph.getNodes()[i]);
			// print data to file and screen
			printRoutingMatrix(newGraph);
		}
	}
	
	/**
	 * Method to print the header of the routing
	 * matrix
	 * @param graph
	 */
	public static void printHeader(Graph graph) {
		try (FileWriter fileWriter = new FileWriter(output);
			    PrintWriter printWriter = new PrintWriter(fileWriter)) 
		{
			printWriter.printf("%15s", " ");
			System.out.printf("%15s", " ");
			// go through each unique node in the graph
			for (Node n : graph.getNodes()) {
				// print to file and screen
				printWriter.printf("%-10s", n.getName());
				System.out.printf("%-10s", n.getName());
			}
			// print graph separating line
			printWriter.print("\n" + "-".repeat(
					graph.getNodes().length * 10 + 10));
			System.out.print("\n" + "-".repeat(
					graph.getNodes().length * 10 + 10));

		}
		catch(Exception e) {};
	}
	
	/**
	 * Method to print the routing matrix neatly to 
	 * the console and output file
	 * @param graph
	 */
	public static void printRoutingMatrix(Graph g) {

		try (FileWriter fileWriter = new FileWriter(output, true); PrintWriter writer = new PrintWriter(fileWriter)) {
			// print the first element
			String format = "%n%-10s%s";
			writer.printf(format, g.getSource(), "|    ");
			System.out.printf(format, g.getSource(), "|    ");

			// print each node in the graph
			for (Node n : g.getNodes()) {
				Node first = n.firstPathNode(g.getSource()) == null ? n : n.firstPathNode(g.getSource());
				// set format
				format = "%-10s";

				// print node info
				if (n.getDistance() == 0) {
					writer.printf(format, "-");
					System.out.printf(format, "-");
				} else {
					writer.printf(format, first.toString() + "," + n.getDistance());
					System.out.printf(format, first.toString() + "," + n.getDistance());
				}
			}
		} catch (Exception e) {}
	}

	/**
	 * Method uses djikstra's algorithm to find all closest paths from a node
	 * @param graph
	 * @param source
	 * @return changed graph
	 */
	public static Graph djikstra(Graph graph, Node source) {
		ArrayList<Node> settled = new ArrayList<>(); // unexplored nodes
		ArrayList<Node> unsettled = new ArrayList<>(); // explored nodes

		// set distance to self 0
		source.setDistance(0);
		unsettled.add(source); // add source to unexplored

		// go through all unsettled nodes
		while (unsettled.size() > 0) {
			// get the closest node and check path
			Node curr = findClosestNode(unsettled);

			// check all all adjacent nodes for next closest path
			for (Node node : curr.getAdjacentNodes().keySet()) {
				Node adjacentN = node;
				Integer weight = curr.getAdjacentNodes().get(node);

				if (!settled.contains(adjacentN)) {
					getMinEdge(adjacentN, weight, curr);
					unsettled.add(adjacentN);
				}
			}

			// add to settled nodes
			unsettled.remove(curr);
			settled.add(curr);
		}
		return graph;
	}

	/** Method to get the minimum edge distance
	 *  to a node adjacent to the source node
	 * @param evalNode
	 * @param weight
	 * @param source
	 */
	private static void getMinEdge(Node evalNode, Integer weight, Node source) {
		// check if the distance from source to edge is
		// less than the current shortest distance to edge
		if (source.getDistance() + weight < evalNode.getDistance()) {
			evalNode.setDistance(source.getDistance() + weight);
			evalNode.setShortestPath(source);
		}
	}

	/**
	 * Method to find the closest node to the source
	 * @param unsettledNodes
	 * @return
	 */
	private static Node findClosestNode(ArrayList<Node> unsettled) {
		Node lowestNode = null;
		int lowestDistance = Integer.MAX_VALUE;
		
		// check the distance of each unsettled node
		for (Node node : unsettled) {
			if (node.getDistance() < lowestDistance) {
				lowestDistance = node.getDistance();
				lowestNode = node;
			}
		}
		return lowestNode;
	}

	/**
	 * Method checks whether command line args are valid - sets files accordingly.
	 * @param args
	 * @throws Exception
	 */
	public static void checkCommandArgs(String[] args) throws Exception {
		String error = "";

		// check and resolve command line arguments
		for (int i = 0; i < args.length; i += 2) {
			switch (args[i].toLowerCase()) {
			case "-i":
				error = (args.length <= i + 1) ? "Error: enter input file name\n" : "";
				System.out.print(error);
				input = new File(args[i + 1]);
				break;
			case "-o":
				error = (args.length <= i + 1) ? "Error: enter output file name\n" : "";
				System.out.print(error);
				output = new File(args[i + 1]);
				break;
			default:
				throw new Exception();
			}
		}
	}

}

/** 
 * Class to take a file representing a weighted graph
 * and parse it to an actual graph
 * @author azind
 *
 */
class ParseInput {
	File input;

	public ParseInput(File file) {
		this.input = file;
	}

	public Graph parseGraph() {
		Graph parsedGraph = new Graph();
		Set<String> names = new HashSet<>();
		HashMap<String, Node> nodeMap = new HashMap<String, Node>();

		try (Scanner in = new Scanner(input)) {
			int weight;

			// Read each input line and set node values
			while (in.hasNext()) {
				Node[] nodeArr = { new Node(in.next()), new Node(in.next()) };
				weight = in.nextInt();

				// Add nodes if they don't already exist
				for (Node n : nodeArr) {
					if (!names.contains(n.getName())) {
						names.add(n.getName());
						nodeMap.put(n.getName(), n);
					}
				}

				// If nodes exist, update existing links
				Node addNode = nodeMap.get(nodeArr[1].getName());
				nodeMap.get(nodeArr[0].getName()).addLink(addNode == null ? nodeArr[1] : addNode, weight);
			}

			// set graph nodes
			for (Node n : nodeMap.values())
				parsedGraph.addNode(n);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return parsedGraph;
	}
}

/**
 * Class to hold graph information
 * @author Farha Zindah
 *
 */
class Graph {

	private SortedSet<Node> nodes = new TreeSet<>();
	private Node source;

	// default constructor
	public Graph() {}

	// constructor takes an argument
	public Graph(SortedSet<Node> nodes, Node source) {
		this.nodes = nodes;
	}

	// add node
	public void addNode(Node nodeA) {
		nodes.add(nodeA);
	}

	// get node
	public Node getNode(int index) {
		Node[] nodeArr = new Node[nodes.size()];
		nodeArr = nodes.toArray(nodeArr);
		return nodeArr[index];
	}

	// get all nodes in graph
	public Node[] getNodes() {
		TreeSet<Node> set = new TreeSet<Node>(nodes);
		Node[] n = new Node[set.size()];
		return set.toArray(n);
	}

	// set graph source
	public void setSource(Node source) {
		this.source = source;
	}
	
	public Node getSource() {
		return source;
	}

	@Override
	public String toString() {
		return nodes.toString();
	}
}

/**
 * Node class holds node distance, shortest paths, and adjacent nodes
 */
class Node implements Comparable {

	private String name;
	Map<Node, Integer> adjacentNodes = new HashMap<>();
	private Node shortestPath = this;
	private int distance = Integer.MAX_VALUE;
	
	// constructors
	public void addLink(Node destination, int distance) {
		adjacentNodes.put(destination, distance);
		// unweighed graph: add adjacency to both nodes
		destination.setAdjacent(this, distance);
	}

	public Node(String name) {
		this.name = name;
	}

	// getters
	public String getName() {
		return name;
	}

	public Node getShortestPath() {
		return shortestPath;
	}

	public Integer getDistance() {
		return distance;
	}

	public Map<Node, Integer> getAdjacentNodes() {
		return adjacentNodes;
	}

	// setters
	public void setAdjacent(Node n, int distance) {
		adjacentNodes.put(n, distance);
	}

	public void setShortestPath(Node p) {
		shortestPath = p;
	}

	public void setDistance(Integer d) {
		distance = d;
	}

	// get firstPath node 
	public Node firstPathNode(Node src) {
		Node first = shortestPath;
		Node copy = null;
		while (!first.getName().equals(src.getName())) {
			copy = first;
			first = copy.getShortestPath();
		}
		return copy;
	}

	@Override
	public String toString() {
		return name;
	}

	// comparable
	@Override
	public int compareTo(Object o) {
		return this.getName().compareTo(((Node) o).getName());
	}
}
	