package jjoller.foxinabox.playermodels.treesearch.trees;

public interface MCRootNodeFactory {

	MonteCarloTreeNode rootNode(double maxReward, double minReward);
	
}