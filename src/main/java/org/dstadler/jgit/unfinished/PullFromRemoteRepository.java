package org.dstadler.jgit.unfinished;

/*
 * Copyright 2013, 2014 Dominik Stadler
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.PullResult;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.Collection;



/**
 * Note: This snippet is not done and likely does not show anything useful yet
 *
 * @author dominik.stadler at gmx.at
 */
public class PullFromRemoteRepository {

    private static final String REMOTE_URL = "https://github.com/github/testrepo.git";



    public Repository openJGitExistingLocal(File gitDirRoot) throws IOException {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();

        return builder.setGitDir(gitDirRoot).build();
        /*
         * return builder
         * .readEnvironment() // scan environment GIT_* variables
         * .findGitDir() // scan up the file system tree
         * .build();
         */
    }

    public static void main(String[] args) throws IOException, GitAPIException {
        PullFromRemoteRepository app = new PullFromRemoteRepository();
        // jGit needs to be given the .git directory to properly initislize and work
        File f = new File("/Users/tusharkapila/u/w/jgits/lcl1/tgkprog2/testRepo.git6079116562883590399_jgit/.git");
        Repository repo = null;
        Git git = null;
        ObjectReader reader = null;
        ObjectId head = null;
        ObjectId oldHead = null;
        try {
            repo = app.openJGitExistingLocal(f);
            System.out.println("v3 Having repository: " + repo.getDirectory() + " with head: " +
                    repo.findRef(Constants.HEAD) + "/" + repo.resolve("HEAD") + "/" +
                    repo.resolve("refs/heads/master"));
            git = new Git(repo);
            oldHead = repo.resolve("HEAD^{tree}");
            PullCommand pc = git.pull();
            PullResult pr = pc.call();
            System.out.println("Pull result :" + pr + "\n Merge :" + pr.getMergeResult() + ", " + pr.getFetchResult() +
                    ", is succ : " + pr.isSuccessful());
            FetchResult fr = pr.getFetchResult();
            String m1 = fr.getMessages();
            Collection<TrackingRefUpdate> tr1 = fr.getTrackingRefUpdates();
            System.out.println("fetch msg :" + m1);

            for (TrackingRefUpdate tra : tr1) {
                System.out.println("TrackingRefUpdate :" + tra);
                // head = tra.getNewObjectId();
                // oldHead = tra.getOldObjectId();

            }


            head = repo.resolve("HEAD^{tree}");



            reader = repo.newObjectReader();
            CanonicalTreeParser oldTreeIter = new CanonicalTreeParser();
            oldTreeIter.reset(reader, oldHead);
            CanonicalTreeParser newTreeIter = new CanonicalTreeParser();
            newTreeIter.reset(reader, head);
            List<DiffEntry> diffs = git.diff().setNewTree(newTreeIter).setOldTree(oldTreeIter).call();
            for (DiffEntry diff : diffs) {
                String n1 = diff.getNewPath();
                FileMode nmode = diff.getNewMode();
                System.out.println("Nw " + n1 + ", new mode :" + nmode);
            }
        } catch (Exception e) {
            System.err.println("PULL :  git : " + e);
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (Exception e1) {
                System.err.println("ignoreable : closing reader : " + e1);
            }
            try {
                git.close();
            } catch (Exception e1) {
                System.err.println("ignoreable : closing git : " + e1);
            }
            try {
                repo.close();
            } catch (Exception e2) {
                System.err.println("ignoreable : closing repo  : " + e2);
            }
        }
        System.out.println("Done. Exiting Okay.");

    }

    public static void main2(String[] args) throws IOException, GitAPIException {
        // prepare a new folder for the cloned repository
        File localPath = File.createTempFile("TestGitRepository", "");
        if (!localPath.delete()) {
            throw new IOException("Could not delete temporary file " + localPath);
        }

        // then clone
        System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);
        try (Git result = Git.cloneRepository().setURI(REMOTE_URL).setDirectory(localPath).call()) {
            // Note: the call() returns an opened repository already which needs to be closed to avoid file handle leaks!
            System.out.println("Having repository: " + result.getRepository().getDirectory());
            try (Git git = new Git(result.getRepository())) {
                git.pull().call();
            }

            System.out.println("Pulled from remote repository to local repository at " + result.getRepository().getDirectory());
        }
    }
}
