package com.duole.service.download;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.duole.Duole;
import com.duole.pojos.asset.Asset;
import com.duole.service.download.dao.FileOperator;
import com.duole.utils.Constants;

import android.R.integer;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class FileMultiThreadDownloader extends Thread{
	private static final String TAG = "FileMultiThreadDownloader";
	private Context context;
	private FileOperator fileService;
	
	private OnDownloadCompleteListener odcl;
	private OnDownloadErrorListener odel;
	/* �������ļ����� */
	private int downloadSize = 0;
	private int preSize = 0;
	/* ԭʼ�ļ����� */
	private int fileSize = 0;
	/* �߳��� */
	private FileDownloadThread[] threads;
	/* ���ر����ļ� */
	private File saveFile;
	private File targetFile;
	private Asset in;
	/* ������߳����صĳ��� */
	private Map<Integer, Integer> data = new ConcurrentHashMap<Integer, Integer>();
	/* ÿ���߳����صĳ��� */
	private int block;
	/* ����·�� */
	private String downloadUrl;
	
	private int retryCounts = 0;
	
	public void setOnDownloadCompleteListener(OnDownloadCompleteListener odc){
		odcl = odc;
	}
	
	public void setOnDownloadErrorListener(OnDownloadErrorListener odc){
		odel = odc;
	}

	/**
	 * ��ȡ�߳���
	 */
	public int getThreadSize()
	{
		return threads.length;
	}

	/**
	 * ��ȡ�ļ���С
	 * 
	 * @return
	 */
	public int getFileSize()
	{
		return fileSize;
	}

	/**
	 * �ۼ������ش�С
	 * 
	 * @param size
	 */
	protected synchronized void append(int size)
	{
		downloadSize += size;
	}

	/**
	 * ����ָ���߳�������ص�λ��
	 * 
	 * @param threadId
	 *            �߳�id
	 * @param pos
	 *            ������ص�λ��
	 */
	protected void update(int threadId, int pos)
	{
		this.data.put(threadId, pos);
	}

	/**
	 * �����¼�ļ�
	 */
	protected synchronized void saveLogFile()
	{
		this.fileService.update(this.downloadUrl, this.data);
	}

	public FileMultiThreadDownloader(Context context, String downloadUrl,
			File fileSaveDir,File target, int threadNum){
		this(context, null, downloadUrl, fileSaveDir, target, threadNum);
	}
	/**
	 * �����ļ�������
	 * 
	 * @param downloadUrl
	 *            ����·��
	 * @param fileSaveDir
	 *            �ļ�����Ŀ¼
	 * @param threadNum
	 *            �����߳���
	 */
	public FileMultiThreadDownloader(Context context,Asset inAsset, String downloadUrl,
			File fileSaveDir,File target, int threadNum)
	{

		Log.d("TAG", downloadUrl);
		targetFile = target;
		in = inAsset;
		try
		{
			this.context = context;
			this.downloadUrl = downloadUrl;
			fileService = new FileOperator(this.context);
			URL url = new URL(this.downloadUrl);
			if (!fileSaveDir.exists())
				fileSaveDir.mkdirs();
			this.threads = new FileDownloadThread[threadNum];
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5 * 1000);
			conn.setRequestMethod("GET");
			conn.setRequestProperty(
					"Accept",
					"image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
			conn.setRequestProperty("Accept-Language", "zh-CN");
			conn.setRequestProperty("Referer", downloadUrl);
			conn.setRequestProperty("Charset", "UTF-8");
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.connect();
//			printResponseHeader(conn);
			if (conn.getResponseCode() == 200)
			{
				this.fileSize = conn.getContentLength();// ������Ӧ��ȡ�ļ���С
				if (this.fileSize <= 0)
					throw new RuntimeException("Unkown file size ");

				String filename = getFileName(conn);
				this.saveFile = new File(fileSaveDir, filename);/* �����ļ� */
				Map<Integer, Integer> logdata = fileService.getData(downloadUrl);
				if (logdata.size() > 0)
				{
					for (Map.Entry<Integer, Integer> entry : logdata.entrySet())
						data.put(entry.getKey(), entry.getValue());
				}
				this.block = (this.fileSize % this.threads.length) == 0 ? this.fileSize/ this.threads.length : this.fileSize / this.threads.length + 1;
				if (this.data.size() == this.threads.length)
				{
					for (int i = 0; i < this.threads.length; i++)
					{
						this.downloadSize += this.data.get(i + 1);
					}
				}
			} else
			{
				throw new RuntimeException("server no response ");
			}
		} catch (Exception e)
		{
			throw new RuntimeException("don't connection this url" + this.downloadUrl);
		}
	}

	/**
	 * ��ȡ�ļ���
	 */
	private String getFileName(HttpURLConnection conn)
	{
		String filename = this.downloadUrl.substring(this.downloadUrl
				.lastIndexOf('/') + 1);
		if (filename == null || "".equals(filename.trim()))
		{// �����ȡ�����ļ�����
			for (int i = 0;; i++)
			{
				String mine = conn.getHeaderField(i);
				if (mine == null)
					break;
				if ("content-disposition".equals(conn.getHeaderFieldKey(i)
						.toLowerCase()))
				{
					Matcher m = Pattern.compile(".*filename=(.*)").matcher(
							mine.toLowerCase());
					if (m.find())
						return m.group(1);
				}
			}
			filename = UUID.randomUUID() + ".tmp";// Ĭ��ȡһ���ļ���
		}
		return filename;
	}

	@Override
	public void run() {

		try {

			RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rw");
			if (this.fileSize > 0)
				randOut.setLength(this.fileSize);
			randOut.close();
			URL url = new URL(this.downloadUrl);
			if (this.data.size() != this.threads.length) {
				this.data.clear();
				for (int i = 0; i < this.threads.length; i++) {
					this.data.put(i + 1, 0);
				}
			}
			for (int i = 0; i < this.threads.length; i++) {
				int downLength = this.data.get(i + 1);
				if (downLength < this.block
						&& this.downloadSize < this.fileSize) { // ���߳�δ�������ʱ,��������
					this.threads[i] = new FileDownloadThread(this, url,
							this.saveFile, this.block, this.data.get(i + 1),
							i + 1);
					this.threads[i].setPriority(7);
					this.threads[i].start();
				} else {
					this.threads[i] = null;
				}
			}
			this.fileService.save(this.downloadUrl, this.data);
			boolean notFinish = true;// ����δ���
			while (notFinish) {// ѭ���ж��Ƿ��������

				Thread.sleep(1000);
				notFinish = false;// �ٶ��������
				for (int i = 0; i < this.threads.length; i++) {
					if (this.threads[i] != null && !this.threads[i].isFinish()) {
						notFinish = true;// ����û�����
						if (this.threads[i].getDownLength() == -1) {// �������ʧ��,����������
							this.threads[i] = new FileDownloadThread(this, url,
									this.saveFile, this.block,
									this.data.get(i + 1), i + 1);
							this.threads[i].setPriority(7);
							this.threads[i].start();

							retryCounts++;
						}
					}
				}

				if (retryCounts > 5) {
					throw new Exception("Too much retries.");
				}
				
				saveLogFile();
				float rate = (this.downloadSize - this.preSize);
				rate = rate / 1024 + rate % 1024;
				
				this.preSize = this.downloadSize;
			}

			fileService.delete(this.downloadUrl);

			odcl.onDownloadComplete(in, saveFile, targetFile);

		} catch (Exception e) {
			e.printStackTrace();

			odel.onError();

			synchronized (this) {
				notify();
			}
		}

		synchronized (this) {
			notify();
		}
		super.run();
	}
}
