package com.generaterun.foo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.utils.io.FileUtils;
import org.apache.maven.shared.utils.io.IOUtil;
/*
 * mvn clean buildJar:zip compile buildJar:jar
 * 
 */
@Mojo(name = "jar", inheritByDefault = true, requiresDependencyResolution = ResolutionScope.COMPILE, threadSafe = true)
public class JarAssemblyMojo extends AbstractMojo {
	// 输入的文件名称
	@Parameter(defaultValue = "temp", required = true)
	private String finalName;
	
	@Parameter( defaultValue = "${project}", readonly = true, required = true )
    private MavenProject project;
	
	@Parameter( defaultValue = "com.ydh.main.Bootstrasp", required = true )
    private String mainClass;

	/**
	 * Location of the file.
	 * 
	 * @parameter expression="${project.build.directory}"
	 * @required
	 */
	//target文件夹
	@Parameter(defaultValue = "${project.build.directory}", required = true)
	private File outputDirectory;

	public void execute() throws MojoExecutionException {
		Log log = this.getLog();
		log.debug("outputDirectory:" + outputDirectory.getAbsolutePath());
		log.debug("finalName:" + finalName);

		if (!outputDirectory.exists()) {
			outputDirectory.mkdirs();
		}

		try {
			File zip=new File(outputDirectory, finalName + ".zip");
			ZipUtil.unZip(zip,outputDirectory);
			// 用于存放解压缩后临时文件的地方
			File temp = new File(outputDirectory, finalName);
			final File output = new File(outputDirectory, "run"); // real file
															// dictional
			FileUtils.forceDelete(output);
			FileUtils.forceMkdir(output);
			
			FileUtils.copyDirectory(new File(temp, "lib"), output);//复制jar包
			File fest=createMainFest(output);//生成MainFest文件
			//利用java打包
			File runJar=new File(output,"run.jar");
			File classes=new File(outputDirectory,"classes");
			removePropertiesAndLog(classes);
			log.info(fest.getAbsolutePath()+":"+fest.exists());
			String cmd="jar cvfm "+getPathInCmd(runJar)+" "+getPathInCmd(fest)+" .";
			log.info(cmd);
			CmdUtils.readConsole(cmd, classes,false);
			//拷贝配置文件
			FileUtils.copyDirectory(temp, new File(output, "conf"),
					"*.properties", null);
			FileUtils.copyDirectory(temp, new File(output, "conf"),
					"logback.xml", null);

			zip.delete();//删除临时文件
		} catch (Exception e1) {
			this.getLog().error(e1.getMessage(), e1);
			throw new MojoExecutionException(e1.getMessage(), e1);
		}

	}

	private String getPathInCmd(File f){
		return f.getAbsolutePath();
	}
	/*
	 * TODO:遍历子文件夹
	 * 移除目录下的properties文件和logback.xml
	 * @param classes
	 *
	 * @author youxia
	 */
	private void removePropertiesAndLog(File classes) {
		File[] files = classes.listFiles();
		List<File> removes=new ArrayList<File>();
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName().toLowerCase();
			if(name.endsWith(".properties")||name.equals("logback.xml")){
				removes.add(files[i]);
			}
		}
		for(File f:removes){
			f.delete();
		}
		
	}

	private String getVersion(){
		return project.getVersion().replace("-SNAPSHOT", "");
	}
	private File createMainFest(File libDir) throws IOException {
		// 修改MANIFEST.MF文件
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
		String dateTime = df.format(new Date());
		StringBuffer presb = new StringBuffer();
		// 抬头不变得文字
		presb.append("Manifest-Version: "+getVersion()+"\r\n");
		presb.append("Created-By: weile\r\n");
		presb.append("Built-Date: " + dateTime + "\r\n");
		presb.append("Implementation-Title: "+project.getArtifactId()+"\r\n");
		presb.append("Implementation-Version: "+getVersion()+"\r\n");
		presb.append("Implementation-Vendor: weile\r\n");
		presb.append("Main-Class: "+mainClass+"\r\n");
		presb.append("Class-Path: ./ conf/ \r\n");
		// 获取工程目录
		File[] files = libDir.listFiles();
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			presb.append(" " + name + " \r\n");
		}
		File fest = new File(this.outputDirectory, "MANIFEST.MF");
		fileWriteImediately(fest, "UTF-8", presb.toString());//需要立即写入到硬盘，否则会因为系统缓存取不到文件
		return fest;
	}
	
	/*
	 * 立即写入到硬盘
	 */
	 private static void fileWriteImediately( @Nonnull File file, @Nullable String encoding, @Nonnull String data )
		        throws IOException		    {
		        Writer writer = null;
		        try
		        {
		        	FileOutputStream out = new FileOutputStream( file );
		            if ( encoding != null )
		            {
		                writer = new OutputStreamWriter( out, encoding );
		            }
		            else
		            {
		                writer = new OutputStreamWriter( out );
		            }
		            writer.write( data );
		            writer.flush();
		            out.getFD().sync();
		        }
		        finally
		        {
		            IOUtil.close( writer );
		        }
		    }

}
