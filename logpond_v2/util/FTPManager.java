package com.infocomm.logpond_v2.util;
import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


public class FTPManager {

	public static String ip = "infocomm.homelinux.com";
	public static int port = 21;
	public static String username = "demo";
	public static String password = "demo";

	/**
	 * Download a single file from the FTP server
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param remoteFilePath path of the file on the server
	 * @param savePath path of directory where the file will be stored
	 * @return true if the file was downloaded successfully, false otherwise
	 * @throws IOException if any network or IO error occurred.
	 */
	public static boolean downloadSingleFile(Context context, FTPClient ftpClient, String remoteFilePath, String savePath) throws IOException {
		File downloadFile = new File(savePath);
		// Tell the media scanner about the new file so that it is
		// immediately available to the user.
		MediaScannerConnection.scanFile(context,
				new String[] { downloadFile.toString() }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {

					}
				});
		File parentDir = downloadFile.getParentFile();
		if (!parentDir.exists()) {
			parentDir.mkdir();
		}

		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
		try {
			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
			return ftpClient.retrieveFile(remoteFilePath, outputStream);
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}
	}

	/**
	 * Download a whole directory from a FTP server.
	 * @param ftpClient an instance of org.apache.commons.net.ftp.FTPClient class.
	 * @param parentDir Path of the parent directory of the current directory being
	 * downloaded.
	 * @param currentDir Path of the current directory being downloaded.
	 * @param saveDir path of directory where the whole remote directory will be
	 * downloaded and saved.
	 * @throws IOException if any network or IO error occurred.
	 */
	public static void downloadDirectory(Context context, FTPClient ftpClient, String parentDir, String currentDir, String saveDir) throws IOException {
		String dirToList = parentDir;
		if (!currentDir.equals("")) {
			dirToList += "/" + currentDir;
		}
		FTPFile[] subFiles = ftpClient.listFiles(dirToList);
		if (subFiles != null && subFiles.length > 0) {
			for (FTPFile aFile : subFiles) {
				System.out.println("downloadDirectorygetName: " + aFile.getName());
				String currentFileName = aFile.getName();
				if (currentFileName.equals(".") || currentFileName.equals("..")) {
					// skip parent directory and the directory itself
					continue;
				}
				String filePath = parentDir + File.separator + currentDir + File.separator + currentFileName;
				if (currentDir.equals("")) {
					filePath = parentDir + File.separator + currentFileName;
				}

				String newDirPath = saveDir + parentDir + File.separator + currentDir + File.separator + currentFileName;
				if (currentDir.equals("")) {
					newDirPath = saveDir + parentDir + File.separator + currentFileName;
				}

				if (aFile.isDirectory()) {
					// create the directory in saveDir
					File newDir = new File(newDirPath);
					boolean created = newDir.mkdirs();
					if (created) {
						System.out.println("CREATED the directory: " + newDirPath);
					} else {
						System.out.println("COULD NOT create the directory: " + newDirPath);
					}
					// Tell the media scanner about the new file so that it is
					// immediately available to the user.
					MediaScannerConnection.scanFile(context,
							new String[] { newDir.toString() }, null,
							new MediaScannerConnection.OnScanCompletedListener() {
								public void onScanCompleted(String path, Uri uri) {

								}
							});

					// download the sub directory
					downloadDirectory(context, ftpClient, dirToList, currentFileName, saveDir);
				} else {
					// download the file
					boolean success = downloadSingleFile(context, ftpClient, filePath, newDirPath);
					if (success) {
						System.out.println("DOWNLOADED the file: " + filePath);
					} else {
						System.out.println("COULD NOT download the file: " + filePath);
					}
				}
			}
		}
	}

	public static boolean uploadFile(FTPClient ftpClient, String localFilePath, String remoteDirPath) {
		FileInputStream fileInputStream = null;
		try {
			File file = new File(localFilePath);
			fileInputStream = new FileInputStream(file);
			ftpClient.changeWorkingDirectory(remoteDirPath);
			boolean status = ftpClient.storeFile(remoteDirPath + File.separator + file.getName(), fileInputStream);
			return status;
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			if(fileInputStream!=null){
				try {
					fileInputStream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static void uploadDirectory(FTPClient ftpClient, String localDirPath, String remoteDirPath, String currentDir) throws IOException {
		if (!currentDir.equals("")) {
			remoteDirPath = currentDir;
		}else{
			File localDir = new File(localDirPath);
			ftpClient.changeWorkingDirectory(remoteDirPath);
			if(!isDirectoryExist(ftpClient, remoteDirPath + File.separator + localDir.getName())){
				createDirectory(ftpClient, remoteDirPath + File.separator + localDir.getName());
			}
			remoteDirPath += File.separator + localDir.getName();
		}
		File localDir = new File(localDirPath);
		File[] files = localDir.listFiles();
		for(int i=0;i<files.length;i++){
			File file = files[i];
			if(file.isDirectory()){
				ftpClient.changeWorkingDirectory(remoteDirPath);
				if(!isDirectoryExist(ftpClient, remoteDirPath + File.separator + file.getName())){
					createDirectory(ftpClient, remoteDirPath + File.separator + file.getName());
				}
				uploadDirectory(ftpClient, file.getAbsolutePath(), remoteDirPath, ftpClient.printWorkingDirectory()+ "/" + file.getName());
			}else{
				uploadFile(ftpClient, file.getAbsolutePath(), remoteDirPath);
			}
		}
	}

	public static boolean validateFileSize(FTPClient ftpClient, String localFilePath, String remoteDirPath) {
		try {
			File file = new File(localFilePath);
			ftpClient.changeWorkingDirectory(remoteDirPath);
			long remoteFileSize = getFileSize(ftpClient, remoteDirPath + File.separator + file.getName());
			return file.length() == remoteFileSize;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private static long getFileSize(FTPClient ftp, String filePath) throws Exception {
		long fileSize = 0;
		FTPFile[] files = ftp.listFiles(filePath);
		if (files.length == 1 && files[0].isFile()) {
			fileSize = files[0].getSize();
		}
		return fileSize;
	}

	public static boolean isDirectoryExist(FTPClient ftpClient, String remoteDirPath) throws IOException {
		return ftpClient.changeWorkingDirectory(remoteDirPath);
	}

	public static boolean createDirectory(FTPClient ftpClient, String remoteDirPath) throws IOException {
		return ftpClient.makeDirectory(remoteDirPath);
	}

	public static boolean isFileExist(FTPClient ftpClient, String remoteFilePath) throws IOException {
		FTPFile[] mFileArray = ftpClient.listFiles(remoteFilePath);
		return mFileArray.length>0;
	}

	public static JSONObject getFileProperties(FTPClient ftpClient, String remoteFilePath) throws IOException {
		JSONObject jsonObject = new JSONObject();
		FTPFile[] mFileArray = ftpClient.listFiles(remoteFilePath);
		if(mFileArray.length>0){
			FTPFile ftpFile = mFileArray[0];
			try {
				jsonObject.put("name", ftpFile.getName());
				jsonObject.put("size", ftpFile.getSize());
				jsonObject.put("timestamp", ftpFile.getTimestamp().getTimeInMillis());
				jsonObject.put("user", ftpFile.getUser());
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return jsonObject;
	}
}
