package org.dstadler.jgit.unfinished;

public class JGitUtls {
	
	//move to io utils, singleton, when refactor
	public static void close(java.lang.AutoCloseable ac){
		try {
			ac.close();
		} catch (Exception e) {
		}
	}

}
