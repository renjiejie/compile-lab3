package dao;

/**
 * 树的遍历
 * @author Administrator
 *
 */  
public class Order {  
    /**
     * 先根遍历
     * @param root 要的根结点
     */  
    public void preOrder(Tree root) {
        if(!root.isEmpty()) {  
            visit(root);
            int childNumber = root.getChilds().size();
            for (int i = 0; i <childNumber; i++) {
                Tree child = root.getChilds().get(childNumber-i-1);
                if(child != null) {
                    child.setNodeDepth(root.getNodeDepth()+1);
                    preOrder(child);
                }
            }
//            for(Tree child : root.getChilds()) {
//                if(child != null) {
//                    child.setNodeDepth(root.getNodeDepth()+1);
//                    preOrder(child);
//                }
//            }
        }  
    }  
    /**
     * 后根遍历
     * @param root 树的根结点
     */  
    public void postOrder(Tree root) {  
        if(!root.isEmpty()) {  
            for(Tree child : root.getChilds()) {  
                if(child != null) {
                    child.setNodeDepth(root.getNodeDepth()+1);
                    preOrder(child);
                }  
            }  
            visit(root);  
        }  
    }  
      
    public void visit(Tree tree) {  
        System.out.println(tree.toString());
    }  
  
}  