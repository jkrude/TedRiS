package teded;

import java.util.ArrayList;
import java.util.List;
import util.Edge;
import util.Node;

public class CheatingDeath {

  public static void main(String[] args) {
    Node[] board = new Node[101];
    List<Edge> edges = new ArrayList<>();
    for (int i = 1; i <= 100; i++) {
      board[i] = new Node(i);
    }
    for (int i = 1; i < 100; i++) {
      for (int j = 1; j <= 6; j++) {
        if (j < i) {
          edges.add(new Edge(board[i], board[i + j]));
        }
      }
    }
    // downwards
    edges.add(new Edge(board[21], board[3]));
    edges.add(new Edge(board[31], board[8]));
    edges.add(new Edge(board[52], board[23]));
    edges.add(new Edge(board[98], board[12]));
    edges.add(new Edge(board[47], board[30]));
    edges.add(new Edge(board[76], board[41]));
    edges.add(new Edge(board[81], board[62]));
    edges.add(new Edge(board[88], board[67]));
    // upwards
    edges.add(new Edge(board[5], board[15]));
    edges.add(new Edge(board[19], board[41]));
    edges.add(new Edge(board[28], board[50]));
    edges.add(new Edge(board[35], board[96]));
    edges.add(new Edge(board[44], board[82]));
    edges.add(new Edge(board[53], board[94]));
    edges.add(new Edge(board[59], board[95]));
    edges.add(new Edge(board[70], board[91]));

  }

}
