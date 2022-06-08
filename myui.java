import static java.nio.file.StandardOpenOption.*;
import static java.nio.file.StandardCopyOption.*;
import java.nio.file.*;
import java.util.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;

public class myui{
	//thinking about having two handlers: one for keeping a record of everything and one
	//for the user to use
	//master handler is something that is just flushed later on where custom handlers
	//could be used to get spefic messages
	//of course this also means that the debugger menu will have to be revamped
	//fix has_parameter bug where null parameter crashes the ui
	//omg so many bugs today! has returns true with any string with x in it for shutdown!
	//6/8/2022
	//would like to just revamp most of the shell so that commands can be entered from
	//.bat and just run; lazyIO is useful for some things, but would like the convenience
	//of just jumping to arrange xml with args you know?

	private static final String LAST_COMPILE= "Last compile: 6/8/2022 0608";
	private static final Logger myui_logger = Logger.getLogger( myui.class.getName() );
	private static final StreamHandler master_handler = new StreamHandler(System.out, new SimpleFormatter() );
	private static final String[] MAIN_OPTIONS = {"fc", "lazyIO", "log", "test_has"};
	private static final String[] MAIN_PARAMETERS = {"-fh"};
	public static final String[] Y_N = {"y", "n"};
	private static final String[] prompts = {"check", "debug", "rename_vinesauce", 
	"dir", "gethtml", "write", "x", "commands", "vidmeta", "getmeta", "arrange_xml", "test"};
	private static final int LAZYIO_GETMETA = 9;
	private static final int LAZYIO_VIDMETA = 8;
	private static final int LAZYIO_ARRANGE_XML = 10;
	private static final int LAZYIO_TEST = 11;
	private static final String[] DEBUG = {"commands", "flush_master", "return",
	"btools_version", "btools_log"};
	private static final int DEBUG_COMMANDS = 0;
	private static final int DEBUG_FLUSH_MASTER = 1;
	private static final int DEBUG_RETURN = 2;
	private static final int DEBUG_BTOOLS_VERSION = 3;
	private static final int DEBUG_BTOOLS_LOG = 4;
	public static final int VIDMETA_NAME = 0;
	public static final int VIDMETA_CHANNEL = 1;
	public static final int VIDMETA_DATE = 2;

	public static void show_pathlist(ArrayList<Path> cabnit){
		//dont forget to add throws IOException in function in the future
		//6/2/2021
		//the assumption here is suppose to be that show_dir is meant to show files and folders separately
		String mname = "show_pathlist";
		for(Path file: cabnit)
				System.out.println((cabnit.indexOf(file)+1)+". "+file.toFile().getName() );
		System.out.println("");
	}

	public static String rename_vinesauce(String entry){
		String mname = "rename_vinesauce";
		myui_logger.fine(mname+"(entry: "+entry+")" );
		StringBuffer rename = new StringBuffer(entry);
		if(btools.has(entry, "Vinesauce") ){
			if(btools.has(entry, "yt1s.com") ){
				rename.delete(0, rename.indexOf("V") );
				rename.delete(rename.indexOf("_"), rename.indexOf(".") );
			}
			if(btools.has_index(rename.toString(), "Vinesauce", true) == 0 ){
				rename.insert(0, '[');
				rename.insert( (btools.has_index(rename.toString(), "Vinesauce", false)+1), ']');
			}
			if(btools.has(rename.toString(), "Vinny") ){
				int d = btools.has_index(rename.toString(), "Vinny", false)+2;
				if(rename.charAt(d) != '-'){
					if(rename.charAt(d)!= '&'){
						rename.insert( (btools.has_index(rename.toString(), "Vinny", false)+1), ' ');
						rename.insert( (btools.has_index(rename.toString(), "Vinny", false)+2), '-');
					}
				}
				else{

					d++;
					if(rename.charAt(d) != ' '){
						rename.insert(d, ' ');
					}
					d++;
					while(!Character.isAlphabetic(rename.charAt(d) ) ){
						if(rename.charAt(d) == '&'){
							int b = btools.has_index(rename.toString(), "Vinny", false)+2;
							rename.delete(b, d);
							d++;
						}else{
							rename.deleteCharAt(d);
						}
					}
				}
			}
			if(btools.has(rename.toString(), "Joel") ){
				rename.insert( (btools.has_index(rename.toString(), "Joel", false)+1), ' ');
				rename.insert( (btools.has_index(rename.toString(), "Joel", false)+2), '-');
			}
			while(rename.charAt(rename.lastIndexOf(".")-1) == ' ' ){
				rename.deleteCharAt(rename.lastIndexOf(".")-1);
			}
			//dtool.display( "btools.has(rename.toString(), \"PART\") == " + btools.has(rename.toString(), "PART") );
			if(btools.has(rename.toString(), "Part") ){
				int begp = btools.has_index(rename.toString(), "Part", true)-1;
				if(rename.charAt(begp) == '(' ){
					begp--;
					while(rename.charAt(begp) != ' '){
						if(!Character.isAlphabetic(rename.charAt(begp) ) ){
							rename.deleteCharAt(begp);
							begp--;
						}
						else{
							break;
						}
					}
				}else{
					rename.insert(begp, '(' );
				}
				int endp = rename.lastIndexOf(".");
				rename.insert(endp, ')' );
				endp--;
				while(endp != begp){
					if(rename.charAt(endp) == ')')
						rename.deleteCharAt(endp);
					endp--;
				}
			}
			else if( btools.has(rename.toString(), "PART") ){
				int begp = btools.has_index(rename.toString(), "PART", true)-1;
				//dtool.display("char at begp:" + rename.charAt(begp) );
				//dtool.display("rename.charAt(begp) == '(' is "+(rename.charAt(begp) == '(') );
				if(rename.charAt(begp) == '(' ){
					begp--;
					while(rename.charAt(begp) != ' '){
						if(!Character.isAlphabetic(rename.charAt(begp) ) ){
							rename.deleteCharAt(begp);
							begp--;
						}
						else{
							break;
						}
					}
				}else{
					rename.insert(begp, '(' );
				}
				int endp = rename.lastIndexOf(".");
				rename.insert(endp, ')' );
				endp--;
				while(endp != begp){
					if(rename.charAt(endp) == ')')
						rename.deleteCharAt(endp);
					endp--;
				}
			}
		}
		return rename.toString();
	}

	public static boolean search(Path a, Path b, boolean rec){
		int max_files = 0;
		int min_files = 0;
		try (DirectoryStream<Path> stream_b = Files.newDirectoryStream(b)){
			for (Path file: stream_b){
				max_files += 1;
			}
		} catch (IOException | DirectoryIteratorException x){
			System.err.println(x);
		}
		try (DirectoryStream<Path> stream_b = Files.newDirectoryStream(b)){
			for (Path file: stream_b){
				min_files += 1;
				if (file.getFileName().toString().equals(a.getFileName().toString())){
					File test = new File(a.toString());
					File test_b = new File(file.toString());
					if (test.isDirectory() == true && test_b.isDirectory() == true){
						if (rec == true){
							if (test.list().length != test_b.list().length){
								return false;
							}
							else if(test.list().length == 0 && test_b.list().length == 0) {
								return true;
							}
							else{
								int max_count = 0;
								int min_count =0;
								DirectoryStream<Path> counting = Files.newDirectoryStream(a);
								for (Path count: counting){
									max_count += 1;
								}
								DirectoryStream<Path> stream_c = Files.newDirectoryStream(a);
								for (Path filec: stream_c){
									min_count += 1;
									if (search(filec, file, true) == false){
										return false;
									}
									else if (min_count == max_count){
										return true;
									}
								}
							}
						}
						else{
							return true;
						}
					}
					else if (test.isFile() == true && test_b.isFile() == true){
						return true;
					}
					else{
						return false;
					}
				}
				else if (max_files == min_files){
					return false;
				}
			}
		} catch (IOException | DirectoryIteratorException x){
			System.err.println(x);
		}
		return false;
	}

	public static void copy(File sourceLocation, File targetLocation) throws IOException {
    	if (sourceLocation.isDirectory()) {
        	if (search(sourceLocation.toPath(), targetLocation.toPath(), false) ==  false){
        		System.out.println("Creating " + sourceLocation.getName() + " at " + targetLocation.toString());
        		File container = new File(targetLocation, sourceLocation.getName());
        		container.mkdir();
        		for (String m : sourceLocation.list()){
        			copy(new File(sourceLocation, m), container);
        		}
        	}else{
        		System.out.println("Moving contents of " + sourceLocation.getName() + " to " + targetLocation.toString());
        		File container = new File(targetLocation, sourceLocation.getName());
        		for (String m : sourceLocation.list()){
        			copy(new File(sourceLocation, m), container);
        		}
        	}
    	} else {
        	if (search(sourceLocation.toPath(), targetLocation.toPath(), false) == false){
        		Path container = Paths.get(targetLocation.toString() + "\\" + sourceLocation.getName());
        		try (InputStream in = new FileInputStream(sourceLocation);
        		OutputStream out = new BufferedOutputStream(Files.newOutputStream(container, CREATE, APPEND))) {
        			System.out.println("Copying " + sourceLocation.getName() + " to " + targetLocation.toString());
        			byte[] buf = new byte[1024];
        			int length;
        			while ((length = in.read(buf)) > 0) {
            			out.write(buf, 0, length);
        			}
        			in.close();
        			out.close();
    			}
        	}
    	}
	}
	public static String gethtml(String website){
		//when this gets ported put an mb limit on the file size 
		String mname = "gethml";
		myui_logger.fine(mname+"(website: " + website + ")" );
		int c;
		URL hp;
		URLConnection hpCon;
		long len;
		StringBuffer product = new StringBuffer();
		InputStream input = null;
		try{
			hp = new URL(website);
			hpCon = hp.openConnection();
			len = hpCon.getContentLengthLong();
			if(len != 0){
				input = hpCon.getInputStream();
				while( ( (c=input.read()) != -1) )
					product.append( (char) c);
			}
			else{
				myui_logger.fine("No content available.");
			}
		}
		catch(MalformedURLException mal){
			myui_logger.warning("An error occurred trying to establish a connection.");
			myui_logger.warning(mal.toString() );
			product = new StringBuffer("error");
		}
		catch(IOException clause){
			myui_logger.warning("An error occurred trying to establish a connection.");
			myui_logger.warning(clause.toString() );
			product = new StringBuffer("error");
		}
		finally{
			try{
				if(input != null)input.close();
			}
			catch(IOException e){
				myui_logger.warning(e.toString() );
			}
		}
		return product.toString();
	}

	public static void write(String input){
		String mname = "write";
		//myui_logger.fine(mname + "(input: " + input + ")" );
		if(input == null){
			myui_logger.warning("input for write command was null");
			return;
		}
		byte buf[] = input.getBytes();
		String naming = new String("myui_write.txt");
		File product = new File(naming);
		FileOutputStream fu = null;
		try{
			for(int a=1;product.exists();a++){
				naming = "myui_write(" + a + ").txt";
				product = new File(naming);
			}
			fu = new FileOutputStream(product);
			fu.write(buf);
			myui_logger.fine("product.exists() = "+product.exists() );
			myui_logger.fine("the path of product is \n" + product.getCanonicalPath() );
		}
		catch(SecurityException sec){
			myui_logger.warning(sec.toString() );
		}
		catch(IOException ex){
			myui_logger.warning(ex.toString() );
		}
		finally{
			try{
				if(fu != null)fu.close();
			}
			catch(IOException e){
				myui_logger.warning(e.toString() );
			}
		}

	}

	public static void write_video_archive(String vid_url){
		String mname = "write_video_archive";
		myui_logger.fine(mname + "(vid_url: " + vid_url + ")" );
		if(vid_url == null){
			myui_logger.warning("vid_url for write_video_archive command was null");
			return;
		}
		//
	}

	public static void write_va_youtube(File dir, String vid_url, String body, 
		String channel, String y_name, String y_date, String y_description) throws IOException, SecurityException {
		String mname = "write_va_youtube";
		myui_logger.fine(mname + "(vid_url: " + vid_url +", "+body+", "+channel+", "+y_name+", "+y_date+", "+y_description+ ")\n File dir: "+dir.getAbsolutePath() );
		StringBuffer contents = new StringBuffer();
		File output;
		FileOutputStream product = null;
		String file_name = y_name.replaceAll("&quot;","");
		if(y_name != null){
			output = new File(dir.getAbsolutePath()+"\\" + char_char(file_name)+".xml" );
			contents.append("<"+char_char(file_name)+"\n" );
			if(vid_url != null){
				contents.append("\txmlns:y=\"" + vid_url +"\">\n");
			}
			else{
				myui_logger.warning("vid_url in "+mname+" was null");
				return;
			}
		}
		else{
			myui_logger.warning("y_name in "+mname+" was null");
			return;
		}
		contents.append("\t<archive_type>\n");
		contents.append("\t\tvideo\n");
		contents.append("\t</archive_type>\n");
		contents.append("\t<archive_date00>\n");
		contents.append("\t\t"+new Date().toString()+"\n" );
		contents.append("\t</archive_date00>\n");
		if(body != null){
			contents.append("\t<body00>\n");
			contents.append("\t\t"+body+"\n");
			contents.append("\t</body00>\n");
		}
		if(channel != null){
			contents.append("\t<y:channel>\n");
			contents.append("\t\t"+channel+"\n");
			contents.append("\t</y:channel>\n");
		}
		contents.append("\t<y:name>\n");
		contents.append("\t\t"+y_name+"\n");
		contents.append("\t</y:name>\n");
		if(y_date != null){
			contents.append("\t<y:date>\n");
			contents.append("\t\t"+y_date+"\n");
			contents.append("\t</y:date>\n");
		}
		if(y_description != null){
			contents.append("\t<y:description>\n");
			contents.append("\t\t"+y_description+"\n");
			contents.append("\t</y:description>\n");
		}
		contents.append("</"+char_char(file_name)+">" );
		product = new FileOutputStream(output);
		product.write(contents.toString().getBytes() );
		myui_logger.fine("product.exists() = "+output.exists() );
		myui_logger.fine("the path of product is \n" + output.getCanonicalPath() );
	}

	public static String getmeta(String input){
		String mname = "getmeta";
		myui_logger.fine(mname+"(input: " + input + ")" );
		StringBuffer product = new StringBuffer();
		URL hp;
		URLConnection hpCon;
		long len;
		try{
			hp = new URL(input);
			hpCon = hp.openConnection();
			len = hpCon.getDate();
			product.append(new Date(len).toString() );
			product.append('\n');
			len = hpCon.getExpiration();
			product.append(new Date(len).toString() );
			product.append('\n');
			len = hpCon.getLastModified();
			product.append(new Date(len).toString() );
			product.append('\n');
		}
		catch(MalformedURLException mal){
			myui_logger.warning("An error occurred trying to establish a connection.");
			myui_logger.warning(mal.toString() );
			product = new StringBuffer("error");
		}
		catch(IOException clause){
			myui_logger.warning("An error occurred trying to establish a connection.");
			myui_logger.warning(clause.toString() );
			product = new StringBuffer("error");
		}
		return product.toString();
	}

	public static String get_vidmeta(String u){
		StringBuffer product = new StringBuffer("error");
		String vid = gethtml(u);
		if(vid.matches("error") )
			return "There was an error getting the URL; check handle logs";
		if(btools.has(u, "https://youtu.be/") | btools.has(u, "https://www.youtube.com/") ){
			product = new StringBuffer();
			for(int cr = vid.indexOf("<title>") + 7;cr<vid.indexOf(" - YouTube</title>");cr++)
				product.append(vid.charAt(cr) );
			product.append('\n');
			for(int cr = vid.indexOf("<link itemprop=\"name\" content=\"")+31;!btools.has( vid.substring(cr, cr+9),"</span>");cr++)
				product.append(vid.charAt(cr) );
			product.append('\n');
			for(int cr = vid.indexOf("\"dateText\":{\"simpleText\":\"")+26;vid.charAt(cr) != '\"';cr++)
				product.append(vid.charAt(cr) );
		}
		return product.toString();
	}

	public static String get_vidmeta(String u, int v){
		String mname = "get_vidmeta";
		myui_logger.fine(mname+"(u: "+u+", v: "+v+")");
		StringBuffer product = new StringBuffer("error");
		String vid = gethtml(u);
		if(vid.matches("error") ){
			myui_logger.fine(mname+" returned "+product.toString() );
			return "There was an error getting the URL; check handle logs";
		}
		product = new StringBuffer();
		switch(v){
			case VIDMETA_NAME:
				for(int cr = vid.indexOf("<title>") + 7;cr<vid.indexOf(" - YouTube</title>");cr++)
					product.append(vid.charAt(cr) );
				break;
			case VIDMETA_CHANNEL:
				for(int cr = vid.indexOf("<link itemprop=\"name\" content=\"")+31;!btools.has( vid.substring(cr, cr+9),"</span>");cr++)
					product.append(vid.charAt(cr) );
				break;
			case VIDMETA_DATE:
				for(int cr = vid.indexOf("\"dateText\":{\"simpleText\":\"")+26;vid.charAt(cr) != '\"';cr++)
					product.append(vid.charAt(cr) );
				break;
		}
		myui_logger.fine(mname+" returned "+product.toString() );
		return product.toString();
	}

	public static String[] get_vmarray(String u){
		//6/8/2022
		//for starters all calls to get url data should be part of a stream,
		//not stored in a string
		String mname = "get_vidmeta";
		myui_logger.fine(mname+"(u: "+u+")");
		String[] product = new String[3];
		String vid = gethtml(u);
		if(vid.matches("error") ){
			myui_logger.fine(mname+" returned "+product.toString() );
			product[0] = "There was an error getting the URL; check handle logs";
			product[1] = "There was an error getting the URL; check handle logs";
			product[2] = "There was an error getting the URL; check handle logs";
		}
		else{
			for(int n=0;n<product.length;n++){
				switch(n){
					case VIDMETA_NAME:
						StringBuffer name = new StringBuffer();
						for(int cr = vid.indexOf("<title>") + 7;cr<vid.indexOf(" - YouTube</title>");cr++)
							name.append(vid.charAt(cr) );
						product[n] = name.toString();
						break;
					case VIDMETA_CHANNEL:
						StringBuffer channel = new StringBuffer();
						for(int cr = vid.indexOf("<link itemprop=\"name\" content=\"")+31;!btools.has( vid.substring(cr, cr+9),"</span>");cr++)
							channel.append(vid.charAt(cr) );
						product[n] = channel.toString();
						break;
					case VIDMETA_DATE:
						StringBuffer date = new StringBuffer();
						for(int cr = vid.indexOf("\"dateText\":{\"simpleText\":\"")+26;vid.charAt(cr) != '\"';cr++)
							date.append(vid.charAt(cr) );
						product[n] = date.toString();
						break;
				}
			}
		}
		return product;
	}

	public static String print_pathlist(ArrayList<Path> target){
		StringBuffer product = new StringBuffer();
		for(Path p:target)
			product.append(p.toFile().getName() );
		return product.toString();
	}

	public static boolean illegal_file_chara(char c){
		//a future method for this would be to include Windows system strings for file names
		return (c=='^'|c=='~'|c=='`'|c=='\''|c=='\"'|c=='%'|c=='/'|c==' '|c=='!'|c=='?'|c=='#'|c=='<'|c=='>'|c=='*'|c==':'|c=='@'|c=='&'|c=='$'|c=='|'|c=='='|c=='{'|c=='}'|c=='\\'|c=='+');
	}

	public static boolean illegal_file_chara(String s){
		for(char c: s.toCharArray() ){
			if(illegal_file_chara(c) )
				return true;
		}
		return false;
	}

	public static String char_char(String target){
		StringBuffer product = new StringBuffer();
		for(char c: target.toCharArray() ){
			if(!illegal_file_chara(c) ){
				product.append(c);
			}
		}
		return product.toString();
	}

	public static void arrange_xml(Scanner r, String[] para, int start){
		String mname = "arrange_xml";
		StringBuffer stringBuff = new StringBuffer(mname+"(start int: "+start+", number of parameters: "+para.length+", list of args:...)");
		File archive_dir = new File(System.getProperty("user.dir") );
		for(String s: para){
			stringBuff.append("\n"+s);
			if(btools.has(s, "-d:") ){
				archive_dir = new File(s.substring(3) );
				if(!archive_dir.exists() ){
					myui_logger.warning("arrange_xml directory parameter does not exist!\n"+archive_dir.getAbsolutePath() );
					return;
				}
				else{
					myui_logger.fine("a directory was set for arrange_xml:\n"+archive_dir.getAbsolutePath() );
				}
			}
		}
		myui_logger.fine(stringBuff.toString() );
		stringBuff = new StringBuffer();
		if(start+1 == para.length){
			System.out.println("arrange_xml needs a plain text parameter located in the working directory.");
			myui_logger.fine(mname + " exited from lack of plain text parameter!");
		}
		else{
			String file_name = para[start+1];
			if(file_name.indexOf(".txt") == -1){
				System.out.println("The file must be plain text format.");
				return;
			}
			else{
				File text_list = new File(file_name);
				if(!text_list.exists() ){
					myui_logger.warning(mname+" exited because text_list.exists() returned false.\n"+text_list.getAbsolutePath() );
					return;
				}
				else{
					System.out.println(file_name+" was found!");
					System.out.println(text_list.getAbsolutePath() );
					try(BufferedReader vid_urls = new BufferedReader(new FileReader(file_name) ) ){
						String get_url;
						String[] vidmeta_data = new String[3];
						while( (get_url = vid_urls.readLine() ) != null){
							System.out.println("Next link is: "+get_url);
							vidmeta_data = get_vmarray(get_url);
							for(String s: vidmeta_data)
								System.out.println(s);
							System.out.println("Spitting out an xml file..." );
							System.out.println(char_char(vidmeta_data[0] )+".xml" );
							write_va_youtube(archive_dir, get_url, null, vidmeta_data[1], vidmeta_data[0], vidmeta_data[2], null);
							if(r.nextLine().matches("x") )
								System.exit(0);
						}
					}
					catch(SecurityException sec){
						myui_logger.warning(sec.toString() );
						System.out.println(sec.toString() );
						return;
					}
					catch(IOException ex){
						myui_logger.warning(ex.toString() );
						System.out.println(ex.toString() );
						return;
					}
				}
			}
		}
		//String file_name = btools.has_parameter(prompts[LAZYIO_ARRANGE_XML], entry, -1);
	}

	public static void debug_menu(Scanner r){
		String e;
		System.out.println("You are now in the debug menu for myui.");
		do{
			e = r.nextLine();
			switch(btools.has_option(DEBUG, e ) ){
				case DEBUG_COMMANDS:
					for(String c: DEBUG)
						System.out.println(c);
					break;
				case DEBUG_FLUSH_MASTER:
					master_handler.flush();
					break;
				case DEBUG_BTOOLS_VERSION:
					System.out.println(btools.VERSION);
					break;
				case DEBUG_BTOOLS_LOG:
					for(String s: btools.LOG)
						System.out.println(s);
					break;
			}
		}while(!e.toLowerCase().matches(DEBUG[2]) );
		System.out.println("Exiting debug menu");
	}

	public static void lazyIO(Scanner r, String entry){
		//6/2/2021
		//give out some options in the begining to help bring together lazyIO
		//5/25/2022
		//there is currently a problem with the way 'has' picks up on commands
		//arrange_xml gets picked up as 'x' which closes the program
		//i think 'has_parameter' needs to be debugged
		String mname = "lazyIO";
		myui_logger.fine(mname+"(Scanner r, entry: "+entry);
		Path video_media = Paths.get(System.getProperty("user.dir") );
		ArrayList<Path> folders;
		ArrayList<Path> vids;
		System.out.println("LazyIO: type in \"commands\" for options on moving videos or editing settings.");
		do{
			entry = r.nextLine();
			for(String p: btools.has_parameter("-1", entry) )
				myui_logger.fine("btools.has_parameter = "+p);
			myui_logger.fine("btools.has_option = " + btools.has_option(prompts, btools.has_parameter("-1", entry, 0) ) );
			switch( btools.has_option(prompts, btools.has_parameter("-1", entry, 0) ) ){
				case 1:
					debug_menu(r);
					break;
				case 0:
					//think in the future extract_folders needs to throw exceptions
					//extract folders needs to test .exists() to put it in the array
					folders = btools.extract_folders(video_media);
					vids = btools.extract_files(video_media);
					System.out.println("---Showing folders---");
					System.out.println(print_pathlist(folders) );
					System.out.println("Counted "+folders.size()+" video media directories.");
					System.out.println("Counted "+vids.size()+" files.");
					break;
				case 2:
					//rename vinesauce will use the method rename_vinesauce to make changes to a
					//file string and spit out another string, then the case simply renames the
					//file it got the original name from to the string from rename_vinesauce
					System.out.println("Getting videos in "+video_media.toString() );
					vids = btools.extract_files(video_media);
					System.out.println("Renaming Vinesauce vids to remove yts.com and 360p.");
					for(Path p:vids){
						if(btools.has(p.toFile().getName(), "Vinesauce") ){
							String rename = rename_vinesauce(p.toFile().getName() );
							if(!btools.has(p.toFile().getName(), rename) ){
								System.out.println(p.toFile().getName() );
								System.out.println("rename to");
								System.out.println(rename);
								p.toFile().renameTo(new File(video_media.toString()+"\\"+rename ) );
							}
						}
					}
					break;
				case 3:
					if(entry.length() > 3){
						if(entry.charAt(4) == ' '){
						}
					}else{
						System.out.println("Enter the directory lazyIO should access.");
						entry = r.nextLine();
						Path path_dir = Paths.get(entry);
						if(path_dir.toFile().exists() ){
							if(path_dir.toFile().isDirectory() ){
								video_media = Paths.get(entry);
								System.out.println("The video media directory will now be: "+video_media.toString() );
							}else{
								System.out.println("The path entered is a file. You must point to a directory.");
							}
						}else{
							System.out.println("The path entered does not exist.");
						}
					}
					break;
				case 4:
					for(String p: btools.has_parameter(prompts[4], entry) )
						System.out.println(gethtml(p) );
					break;
				case 5:
					int count = 0;
					String[] contain = btools.has_parameter(prompts[5], entry);
					for(String s: contain)
						myui_logger.fine(s);
					if(contain[0].equals(prompts[5]) ){
						System.out.println("write needs an argument separte by a space");
					}
					else{
						for(int n=0;n<contain.length;n++){
							if(contain[n].equals(prompts[4] ) ){
								String[] ch = btools.has_parameter(prompts[4], entry);
								write(gethtml(ch[0]) );
								count++;
								n++;
							}
							else{
								write(contain[n]);
								count++;
							}
						}
					}
					if(count > 0)
						System.out.println(count + " files have been written.");
					break;
				case 6:
					System.exit(0);
					break;
				case 7:
					for(String c: prompts)
						System.out.println(c);
					break;
				case LAZYIO_VIDMETA:
					//"dateText":{"simpleText":"Nov 18, 2021"} +26
					for(String p: btools.has_parameter(prompts[LAZYIO_VIDMETA], entry) ){
						System.out.println(get_vidmeta(p) );
					}
					break;
				case LAZYIO_GETMETA:
					System.out.println("get meta entry");
					for(String p: btools.has_parameter(prompts[LAZYIO_GETMETA], entry) )
						System.out.println(getmeta(p) );
					break;
				case LAZYIO_ARRANGE_XML:
					String file_name = btools.has_parameter(prompts[LAZYIO_ARRANGE_XML], entry, -1);
					myui_logger.fine("file name: "+file_name);
					if(file_name.indexOf(".txt") == -1){
						System.out.println("The file must be plain text format.");
						break;
					}
					File text_list = new File(file_name);
					if(!text_list.exists() ){
						System.out.println(file_name+" does not exist in the current directory!");
						System.out.println(text_list.getAbsolutePath() );
						break;
					}
					else{
						System.out.println(file_name+" was found!");
						System.out.println(text_list.getAbsolutePath() );
						try(BufferedReader vid_urls = new BufferedReader(new FileReader(file_name) ) ){
							String get_url;
							String[] vidmeta_data = new String[3];
							while( (get_url = vid_urls.readLine() ) != null){
								System.out.println("Next link is: "+get_url);
								vidmeta_data = get_vmarray(get_url);
								for(String s: vidmeta_data)
									System.out.println(s);
								System.out.println("Spitting out an xml file..." );
								System.out.println(char_char(vidmeta_data[0] )+".xml" );
								write_va_youtube(new File(System.getProperty("user.dir") ), get_url, null, vidmeta_data[1], vidmeta_data[0], vidmeta_data[2], null);
								if(r.nextLine().matches("x") )
									System.exit(0);
							}
						}
						catch(SecurityException sec){
							myui_logger.warning(sec.toString() );
							System.out.println(sec.toString() );
							break;
						}
						catch(IOException ex){
							myui_logger.warning(ex.toString() );
							System.out.println(ex.toString() );
							break;
						}
					}
					break;
				case LAZYIO_TEST:
					System.out.println("Testing method illegal_file_chara");
					String LAZYIO_TEST_STRING1 = new String("thisdoesnotcontainthosecharacters");
					String LAZYIO_TEST_STRING2 = new String("this&does have#illegal?characters");
					String LAZYIO_TEST_STRING3 = new String("this does as well");
					System.out.println("\""+LAZYIO_TEST_STRING1+"\" has illegal characters: "+illegal_file_chara(LAZYIO_TEST_STRING1) );
					System.out.println("\""+LAZYIO_TEST_STRING2+"\" has illegal characters: "+illegal_file_chara(LAZYIO_TEST_STRING2) );
					System.out.println("\""+LAZYIO_TEST_STRING3+"\" has illegal characters: "+illegal_file_chara(LAZYIO_TEST_STRING3) );
					break;
				default:
					System.out.println("LazyIO: type in \"commands\" for options on moving videos or editing settings.");
					break;
			}
		}while(!btools.has(entry.toLowerCase(), "return") );
		System.out.println("end of "+mname);
	}
	//so building a text prompt seems really difficult...my first idea was about making lazyIO into
	//a thread so that it can be shutoff externally by other functions, including main
	//so now i must embark on a journey to discover more uses of threads
	//so text prompt should maybe not involve sending in Strings to be displayed as part of the function
	//there should be an input string for options though
	//for starters maybe making a string input go through an array
	//so instead of the main_command arguement, we can create an array that contains the options
	//maybe the main problem is that a return value needs to be caught within a function
	//alright so after editing some settings i found that everything after java 'class' in
	//bat stores as an args[] in main
	//so then: java myui arrange_xml would be args[0] = arrange_xml
	public static void main(String [] args){
		Scanner reader = new Scanner(System.in);
		master_handler.setLevel(Level.ALL);
		myui_logger.setLevel(Level.ALL);
		FileHandler file_handler;
		if(args.length > 0){
			for(int n=0;n<args.length;n++){
				String inputForArgs = args[n];
				switch(inputForArgs){
					case "-fh":
						try{
							file_handler = new FileHandler("fh.log");
							file_handler.setLevel(Level.ALL);
							myui_logger.addHandler(file_handler);
							myui_logger.fine("btools.has_option(prompts, " +inputForArgs+") ) = " + btools.has_option(prompts, inputForArgs) );
						}
						catch(IOException e){
							System.out.println("An IO error occurred while declaring a FileHandler");
							System.out.println(e);
							System.exit(0);
						}
						catch(SecurityException se){
							System.out.println("A security error occured while declaring a FileHandler");
							System.out.println(se);
							System.exit(0);
						}
						System.out.println("file_handler was loaded.");
						break;
					default:
						myui_logger.fine("btools.has_option(prompts, " +inputForArgs+") ) = " + btools.has_option(prompts, inputForArgs) );
						switch(btools.has_option(prompts, inputForArgs) ){
							case LAZYIO_ARRANGE_XML:
								arrange_xml(reader, args, n);
								System.exit(0);
						}
						break;
				}
			}
		}
		System.out.println("Welcome to myui.");
		System.out.println(LAST_COMPILE);
		System.out.println("Enter command:");
		String entry = reader.nextLine();
		switch (entry){
			case "fc":
				boolean[] modes = {false, false};
				if (entry.length() > 2 && entry.indexOf('-') > -1){
					if (btools.has(entry.toLowerCase().trim(), "-r")){
						modes[0] = true;
						System.out.println("Recursive is on.");
					}
				}
				System.out.println("Enter primary directory:");
				entry = reader.nextLine();
				Path dir_a = Paths.get(entry);
				System.out.println("Enter comparable directory:");
				entry = reader.nextLine();
				Path dir_b = Paths.get(entry);
				List <Path> sync = new ArrayList <Path>();
				List <Path> unsync = new ArrayList <Path>();
				try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir_a)){
					for (Path file: stream){
						if (search(file, dir_b, modes[0]) == true){
							sync.add(file);
						}else{
							unsync.add(file);
						}
					}
					System.out.println("\n" + "------Showing synced items------");
					for (Path c : sync){
						System.out.println(c.getFileName());
					}
					System.out.println("\n" + "------Showing unsynced items------");
					for (Path c : unsync){
						System.out.println(c.getFileName());
					}
					System.out.println("\nWould you like to copy unsync items over y/n?");
					entry = reader.nextLine();
					if (entry.equals("y")){
						for (Path move : unsync){
							copy(move.toFile(), dir_b.toFile());
						}
					}
				} catch (IOException | DirectoryIteratorException x){
					System.err.println(x);
				}
				break;
			case "lazyIO":
				lazyIO(reader, entry);
				break;
			default:
				System.out.println("Command error; no command exists.");
				break;
		}
	}
}