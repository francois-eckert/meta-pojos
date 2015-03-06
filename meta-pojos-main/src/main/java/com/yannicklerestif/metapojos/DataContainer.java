package com.yannicklerestif.metapojos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.springframework.stereotype.Service;

import com.yannicklerestif.metapojos.elements.beans.DBClass;
import com.yannicklerestif.metapojos.elements.beans.DBMethod;
import com.yannicklerestif.metapojos.elements.beans.DBMethod.DBMethodKey;

@Service
public class DataContainer {

	private Map<String, DBClass> classes = new HashMap<String, DBClass>();

	// ------------------------------------------------------------------------------------
	// container
	// ------------------------------------------------------------------------------------

	DBMethod getOrCreateDBMethod(String className, String methodName, String desc) {
		DBClass dbClass = getOrCreateDBClass(className);
		DBMethodKey key = new DBMethodKey(methodName, desc);
		DBMethod dbMethod = dbClass.getMethods().get(key);
		if (dbMethod == null) {
			dbMethod = new DBMethod();
			dbMethod.setDBClass(dbClass);
			dbMethod.setName(methodName);
			dbMethod.setDesc(desc);
			dbClass.getMethods().put(key, dbMethod);
		}
		return dbMethod;
	}

	DBClass getOrCreateDBClass(String name) {
		DBClass dbClass = classes.get(name);
		if (dbClass == null) {
			dbClass = new DBClass();
			dbClass.setName(name);
			classes.put(name, dbClass);
		}
		return dbClass;
	}

	// ------------------------------------------------------------------------------------
	// class reading
	// ------------------------------------------------------------------------------------

	public void storeClasses(String[] classesJarOrDirectories) throws Exception {
		for (int i = 0; i < classesJarOrDirectories.length; i++) {
			File classesJarOrDirectory = new File(classesJarOrDirectories[i]);
			if (classesJarOrDirectory.isDirectory())
				storeClassDirectory(classesJarOrDirectory);
			else if (classesJarOrDirectory.getName().endsWith(".jar"))
				storeJarFile(classesJarOrDirectory);
			else
				throw new IllegalArgumentException("Not a jar file or a folder : "
						+ classesJarOrDirectory.getAbsoluteFile());
		}
	}

	private void storeClassDirectory(File root) throws Exception {
		File[] list = root.listFiles();
		if (list == null)
			return;
		for (File f : list) {
			if (f.isDirectory()) {
				storeClassDirectory(f);
			} else {
				if (!f.getName().endsWith(".class"))
					continue;
				InputStream stream = new FileInputStream(f);
				processClassInputStream(stream);
			}
		}
	}

	private void storeJarFile(File jarFile_) throws Exception {
		ZipFile jarFile = new ZipFile(jarFile_);
		Enumeration<? extends ZipEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!(entry.getName().endsWith(".class")))
				continue;
			InputStream stream = jarFile.getInputStream(entry);
			processClassInputStream(stream);
		}
		jarFile.close();
	}

	private void processClassInputStream(InputStream stream) throws Exception {
		ClassReader cr = new ClassReader(stream);
		MPClassVisitor visitor = new MPClassVisitor(this);
		cr.accept(visitor, 0);
		// ClassNode cn = new ClassNode();
		// cr.accept(cn, 0);
		// if (cn.name.contains("StartingClass"))
		// System.out.println(cn.name);
	}

}