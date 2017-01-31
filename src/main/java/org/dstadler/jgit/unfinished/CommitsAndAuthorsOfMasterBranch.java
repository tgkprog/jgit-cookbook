package org.dstadler.jgit.unfinished;




import java.io.File;
import java.io.IOException;
import java.util.StringTokenizer;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

/**
 * WIP.
 * 1. Get author info of a file in a branch.  
 * 2. go thru all commits in HEAD and list authors and files.
 * 3. Change sets for a file (lines added, removed or modified)
 * @author Tushar and others 
 * @author dominik.stadler at gmx.at
 * See http://stackoverflow.com/questions/41951609/programmatically-walking-a-jgit-cloned-repository-last-author-or-all-authors
 */
public class CommitsAndAuthorsOfMasterBranch {

    private static final String REMOTE_URL = "https://github.com/tgkprog2/testRepo.git";
    private static final File LOCAL_DIR_ROOT = new File("/Users/tusharkapila/u/w/jgits/lcl1/");

    public static void main(String[] args) throws IOException, GitAPIException {
        CommitsAndAuthorsOfMasterBranch app2 = new CommitsAndAuthorsOfMasterBranch();
        app2.testOpenWalkLocal();

    }

    public void testOpenWalkLocal() {
        PullFromRemoteRepository app = new PullFromRemoteRepository();

        File f = new File("/Users/tusharkapila/u/w/jgits/lcl1/tgkprog2/testRepo.git6079116562883590399_jgit/.git");
        Repository repository = null;
        Git git = null;
        try {
            repository = app.openJGitExistingLocal(f);
            git = new Git(repository);
            System.out.println("v3 Having repository: " + repository.getDirectory() + " with head: " + repository.findRef(Constants.HEAD)
                    + "/" + repository.resolve("HEAD") + "/" + repository.resolve("refs/heads/master"));
            listRepositoryContents(repository, git);
        } catch (Exception e) {
            System.out.println("Tst open local Err: " + e);
            e.printStackTrace();
        }finally {
            JGitUtls.close(git);
            JGitUtls.close(repository);
        }
    }

    //works
    public void cloneIt() throws IOException, GitAPIException {
        // prepare a new folder for the cloned repository
        String repoName = "parseErrorInGetRepoName";
        String projectName = "NotDterminded";

        {
            // TODO get tokens of name and project, from API
            String l1 = null;
            StringTokenizer st = new StringTokenizer(REMOTE_URL, "/");
            while (st.hasMoreTokens()) {
                l1 = repoName;
                repoName = st.nextToken();
            }
            repoName = repoName.replace(".git", "");// remove '.git' from URL end
            projectName = l1;

        }
        File f = new File(LOCAL_DIR_ROOT, projectName);
        f.mkdirs();
        File localPath = File.createTempFile(repoName, "_jgit", f);
        if (!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        Git git = null;
        Repository repository = null;
        try {
            git = jgitCloneToLocal(localPath);
            // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
            System.out.println("Having repository: " + git.getRepository().getDirectory());
            repository = git.getRepository();
            listRepositoryContents(repository, git);
        } catch (Exception e) {
            System.out.println("Cloneit Err: " + e);
            e.printStackTrace();
        } finally {
            JGitUtls.close(git);
            JGitUtls.close(repository);

        }
    }

    private void processRevWalk(RevWalk revWalk, Repository repository) throws IOException{
        Ref head = repository.exactRef("refs/heads/master");
        
        RevCommit revCommit = revWalk.parseCommit(head.getObjectId());
        processRevCommit(revCommit);
        //need to iterate revWalk? How? TODO
    }
    

    private void processRevCommit(RevCommit revCommit){
        try {
            
        
            if(revCommit == null){
                System.out.println("revCommit null");
                return;
            }
            //revCommit = revCommit.g
            /*this throws a
             java.lang.NullPointerException
    at org.eclipse.jgit.util.RawParseUtils.author(RawParseUtils.java:658)
    at org.eclipse.jgit.revwalk.RevCommit.getAuthorIdent(RevCommit.java:406) 
             */
            PersonIdent authi = revCommit.getAuthorIdent();
            if(authi == null){
                System.out.println("authi null");
            }else{
            
                System.out.println(authi.getName() + "|" + authi.getEmailAddress()  + "|" + authi.getWhen());
            }
        } catch ( Exception e) {
            System.out.println(" Err process author in rec vommit : " + e);
            e.printStackTrace();
        }
        //RevTree tree = revCommit.getTree();
        RevCommit[] parents = revCommit.getParents();//
        for(RevCommit parentRevCommit : parents){
            if(parentRevCommit == null){
                System.out.println("parentRevCommit null");
            }else{
                processRevCommit(parentRevCommit);
            }
        }
        //RevCommit commit = walk.parseCommit(head.getObjectId());
        
    }
    
     public void getCommitsByTree( Repository repository , String treeName, Git git) throws Throwable{

            Iterable<RevCommit> revCommits = git.log()
                    .add(repository.resolve(treeName))
                    .call();
            for(RevCommit revCommit : revCommits){
                System.out.println(revCommit.getName());
            }

        }
    
    
    private  void listRepositoryContents(Repository repository, Git git) throws IOException {
        RevWalk revWalk = new RevWalk(repository);
        try {
            processRevWalk(revWalk, repository);
        } catch (Throwable e) {
            System.err.println("Err in rev walk process " + e);
            e.printStackTrace();
        }

        

        // a RevWalk allows to walk over commits based on some filtering that is
        // defined
    
        
        Ref head =  repository.exactRef("refs/heads/master");
        RevCommit commit = revWalk.parseCommit(head.getObjectId());
        RevTree tree = commit.getTree();
        TreeWalk treeWalk = null;
        try {
            treeWalk = new TreeWalk(repository);
            treeWalk.addTree(tree);
            treeWalk.setRecursive(true);
            while (treeWalk.next()) {
                //this works, but how to get history of a file and is this the more performant way or walk the whole repo tree and commits?
                System.out.println("found: " + treeWalk.getPathString());
                //get last commit and person indent of this path. TODO
                //treeWalk.get
            }
        } finally {
            JGitUtls.close(revWalk);
            JGitUtls.close(treeWalk);
        }

    }

    private static Git jgitCloneToLocal(File localPath) throws GitAPIException, InvalidRemoteException, TransportException {
        Git git;
        CloneCommand cloneCmd = Git.cloneRepository();
        git = cloneCmd.setURI(REMOTE_URL).setDirectory(localPath).call();
        return git;
    }
}
