package com.sonatype.sshjgit.core.shiro.session;

import org.apache.mina.core.session.IoSession;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.util.Buffer;
import org.apache.sshd.server.session.ServerSession;

/**
 * This implementation of {@link ServerSession} allows Shiro aware security checks to function correctly.
 *
 * The way a {@link ThreadState} is bound to and cleared from the current thread, was inspired by how it's done in
 * {@code org.apache.shiro.web.servlet.ShiroFilter#doFilterInternal}
 * 
 * @author hugo@josefson.org
 */
class ShiroAwareSshServerSession extends ServerSession {
    private final SecurityManager securityManager;

    public ShiroAwareSshServerSession(SecurityManager securityManager, SshServer sshServer, IoSession ioSession) throws Exception {
        super(sshServer, ioSession);
        this.securityManager = securityManager;
    }

    @Override
    protected void handleMessage(Buffer buffer) throws Exception {
        Subject subject = new Subject.Builder(securityManager).buildSubject();
        ThreadState threadState = new SubjectThreadState(subject);
        threadState.bind();
        try {
            super.handleMessage(buffer);
        } finally {
            threadState.clear();
        }
    }
}
