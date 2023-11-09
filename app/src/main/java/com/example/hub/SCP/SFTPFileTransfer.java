/**
 *
 * @ProjectName: Hub
 * @Package: com.example.hub.SCP
 * @ClassName: SFTPFileTransfer
 * @Description: using a JSch module to use scp protocol to transmit data to the virtual machine
 * @Author: GRP_Team14
 * @CreateDate: 2022/3/14
 * @Version: 1.0
 */

package com.example.hub.SCP;

import com.example.hub.Constant.SFTPConstants;
import com.jcraft.jsch.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;

public class SFTPFileTransfer {

    private String localFile = "";
    private String remoteFile = "";
    private String start_time = "";

    public SFTPFileTransfer(String localFile, String remoteFile, String start_time) {
        this.localFile = localFile;
        long time = Long.parseLong(start_time);
        Date date = new Date(time);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH.mm");
        this.start_time = formatter.format(date);
        this.remoteFile = remoteFile + this.start_time + "/";
    }

    /**
     * Method to upload file to a designated server
     */
    public void upload() {
        Session jschSession = null;

        try {
            JSch jsch = new JSch();
            jschSession = jsch.getSession(SFTPConstants.USERNAME, SFTPConstants.REMOTE_HOST, SFTPConstants.REMOTE_PORT);
            jschSession.setPassword(SFTPConstants.PASSWORD);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            jschSession.setConfig(config);
            jschSession.connect(SFTPConstants.SESSION_TIMEOUT);

            Channel sftp = jschSession.openChannel("sftp");
            sftp.connect(SFTPConstants.CHANNEL_TIMEOUT);
            ChannelSftp channelSftp = (ChannelSftp) sftp;

            try {
                Vector dir = channelSftp.ls(remoteFile);
                if (dir == null) { // if dir is null, then create the directory
                    channelSftp.mkdir(remoteFile);
                }
            } catch (SftpException e) { // if dst dir is null again, catch exception
                channelSftp.mkdir(remoteFile); // create it again.
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            // upload the file
            channelSftp.put(localFile, remoteFile);
            channelSftp.exit();
        } catch (JSchException | SftpException e) {
            e.printStackTrace();
        } finally {
            if (jschSession != null) {
                jschSession.disconnect();
            }
        }
        System.out.println("Data transmitted Finished!");
    }
}
