/*
 * Copyright 2016 haimin.shao.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.relax.mart;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 *
 * @author haimin.shao
 */
public class Dataset {

	public List<Session> sessions;

	public void load(File dataFile) throws FileNotFoundException {
		sessions = new ArrayList();
		Scanner in = new Scanner(dataFile, "utf-8");
		while (in.hasNextLine()) {
			String line = in.nextLine();
			int i = 0;
			i = indexOf(" \t", line);
			double target = Double.parseDouble(line.substring(0, i));
			line = line.substring(i + 1);

			String qid = "";
			if (line.startsWith("qid:")) {
				i = indexOf(" \t", line);
				qid = line.substring(4, i);
				line = line.substring(i + 1);
			}

			Session session;
			if (sessions.isEmpty() || !qid.equals(sessions.get(sessions.size() - 1).id)) {
				session = new Session(sessions.size(), qid);
				sessions.add(session);
			} else {
				session = sessions.get(sessions.size() - 1);
			}

			Instance instance = new Instance(session, session.instances.size(), line);
			session.addExample(instance, target);
		}
		for (Session session : sessions) {
			session.orderByTargetDesc();
		}
	}

	public void dump(File dataFile) throws FileNotFoundException, UnsupportedEncodingException {
		PrintWriter out = new PrintWriter(dataFile, "utf-8");
		for (Session session : this.sessions) {
			for (int i = 0; i < session.instances.size(); i++) {
				Instance instance = session.instances.get(i);
				double target = session.targets.get(i);
				out.print(target);
				out.print(" ");
				if (!session.id.isEmpty()) {
					out.print("qid:" + session.id);
					out.print(" ");
				}
				out.print(instance);
				out.println();
			}
		}
		out.flush();
		out.close();
	}

	private static int indexOf(String delimeters, String line) {
		return indexOf(delimeters, line, 0, line.length());
	}

	private static int indexOf(String delimeters, String line, int from) {
		return indexOf(delimeters, line, from, line.length());
	}

	private static int indexOf(String delimeters, String line, int from, int to) {
		for (int i = from; i < to; i++) {
			char c = line.charAt(i);
			if (delimeters.indexOf(c) >= 0) {
				return i;
			}
		}
		return -1;
	}

	public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {
		File fromFile = new File("D:\\SoftwareData\\rhcygwin64\\home\\haimin.shao\\code\\rankboost_code\\w.dat");
		File toFile = new File("D:\\SoftwareData\\rhcygwin64\\home\\haimin.shao\\code\\rankboost_code\\v.dat");
		Dataset ds = new Dataset();
		ds.load(fromFile);
		ds.dump(toFile);
	}
}
