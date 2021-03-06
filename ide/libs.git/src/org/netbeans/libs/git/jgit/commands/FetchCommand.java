/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.libs.git.jgit.commands;

import org.netbeans.libs.git.GitTransportUpdate;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.libs.git.jgit.DelegatingProgressMonitor;
import org.eclipse.jgit.errors.NotSupportedException;
import org.eclipse.jgit.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.FetchResult;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.URIish;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class FetchCommand extends TransportCommand {

    private final ProgressMonitor monitor;
    private final List<String> refSpecs;
    private final String remote;
    private Map<String, GitTransportUpdate> updates;
    private FetchResult result;
    
    public FetchCommand (Repository repository, GitClassFactory gitFactory, String remoteName, ProgressMonitor monitor) {
        this(repository, gitFactory, remoteName, Collections.<String>emptyList(), monitor);
    }

    public FetchCommand (Repository repository, GitClassFactory gitFactory, String remote, List<String> fetchRefSpecifications, ProgressMonitor monitor) {
        super(repository, gitFactory, remote, monitor);
        this.monitor = monitor;
        this.remote = remote;
        this.refSpecs = fetchRefSpecifications;
    }

    @Override
    protected void runTransportCommand () throws GitException.AuthorizationException, GitException {
        Repository repository = getRepository();
        List<RefSpec> specs = new ArrayList<RefSpec>(refSpecs.size());
        for (String refSpec : refSpecs) {
            specs.add(new RefSpec(refSpec));
        }
        Transport transport = null;
        try {
            transport = openTransport(false);
            transport.setRemoveDeletedRefs(false); // cannot enable, see FetchTest.testDeleteStaleReferencesFails
            transport.setDryRun(false);
            transport.setFetchThin(true);
            transport.setTagOpt(TagOpt.FETCH_TAGS);
            result = transport.fetch(new DelegatingProgressMonitor(monitor), specs);
            processMessages(result.getMessages());
            updates = new HashMap<String, GitTransportUpdate>(result.getTrackingRefUpdates().size());
            for (TrackingRefUpdate update : result.getTrackingRefUpdates()) {
                GitTransportUpdate upd = getClassFactory().createTransportUpdate(transport.getURI(), update);
                updates.put(upd.getLocalName(), upd);
            }
        } catch (NotSupportedException e) {
            throw new GitException(e.getMessage(), e);
        } catch (URISyntaxException e) {
            throw new GitException(e.getMessage(), e);
        } catch (TransportException e) {
            URIish uriish = null;
            try {
                uriish = getUriWithUsername(false);
            } catch (URISyntaxException ex) {
                throw new GitException(e.getMessage(), e);
            }
            handleException(e, uriish);
        } finally {
            if (transport != null) {
                transport.close();
            }
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git fetch ").append(remote); //NOI18N
        for (String refSpec : refSpecs) {
            sb.append(' ').append(refSpec);
        }
        return sb.toString();
    }

    public Map<String, GitTransportUpdate> getUpdates () {
        return Collections.unmodifiableMap(updates);
    }
    
    public FetchResult getResult () {
        return result;
    }
}
