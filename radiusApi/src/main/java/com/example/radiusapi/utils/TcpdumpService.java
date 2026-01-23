package com.example.radiusapi.utils;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

@Service
public class TcpdumpService {

    public void runTcpdump(String user, String password, String host, int port) {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            // Start tcpdump on remote server
            String command = "tcpdump -i ens33 host 192.168.245.128  -w  /abc.pcap\n";
            ChannelExec channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            channel.connect();

            // Wait for a while or implement your logic to stop tcpdump
            Thread.sleep(10000);

            // Stop tcpdump
            channel.disconnect();

            // Transfer pcap file to local machine
            String remoteFile = "/abc.pcap";
            String localFile = "/abc.pcap";
            transferFile(session, remoteFile, localFile);

            // Close the SSH session
            session.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void transferFile(Session session, String remoteFile, String localFile) throws Exception {
        ChannelExec channel = (ChannelExec) session.openChannel("exec");
        channel.setCommand("scp -f " + remoteFile);

        FileOutputStream fos = new FileOutputStream(new File(localFile));
        InputStream in = channel.getInputStream();
        channel.connect();

        byte[] buf = new byte[1024];
        int c;
        while (true) {
            c = in.read(buf, 0, buf.length);
            if (c == 0) break;
            if (c == -1) throw new Exception("Error while transferring file");
            if (c > 0) {
                fos.write(buf, 0, c);
                fos.flush();
            }
        }

        fos.close();
        in.close();
        channel.disconnect();
    }
}
