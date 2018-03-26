package SqlParser;

public class MyIntervalTree {

    public class TreeNode{
        TreeNode left, right;
        NodeData data;

        int max;

        private TreeNode(NodeData data){
            this.data = data;
            this.max = data.getHigh();
            left = right = null;
        }
    }

    static abstract class NodeData{
        public int weight;

        NodeData(){
            weight = 1;
        }

        private void incrementWeight(){
            weight += 1;
        }

        abstract int getHigh();
        abstract int getLow();
        abstract boolean equalTo(NodeData other);
    }

    public static class Point extends NodeData {
        public int data;

        public Point(int data){
            this.data = data;
        }

        public int getHigh() {
            return data;
        }

        public int getLow() {
            return data;
        }

        boolean equalTo(NodeData other) {
            return other.getLow() == data && other.getHigh() == data;
        }

    }

    public static class Interval extends NodeData {
        public int start;
        public int end;

        public Interval(int start, int end){
            this.start = start;
            this.end = end;
        }

        public int getHigh() {
            return end;
        }

        public int getLow() {
            return start;
        }

        boolean equalTo(NodeData other) {
            return other.getHigh() == end && other.getLow() == start;
        }

    }

    private TreeNode root;

    public MyIntervalTree(){
        root = null;
    }

    public TreeNode insert(NodeData data){
        return insert(root, data);
    }

    private TreeNode insert(TreeNode root, NodeData data){
        if(root == null)
            return new TreeNode(data);

        int l = root.data.getLow();

        if(data.getLow() < l){
            root.left = insert(root.left, data);
        }
        else if(root.data.equalTo(data)){
            root.data.incrementWeight();
        }
        else{
            root.right = insert(root.right, data);
        }

        root.max = root.max < data.getHigh() ? data.getHigh() : root.max;

        return root;
    }

}
