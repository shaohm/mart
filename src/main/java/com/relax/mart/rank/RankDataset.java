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
package com.relax.mart.rank;

import com.relax.lib.StringUtils;
import com.relax.mart.Dataset;
import com.relax.mart.Instance;
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
public class RankDataset implements Dataset{

	public List<Session> sessions;

	public void load(File dataFile) throws FileNotFoundException {
		sessions = new ArrayList();
		try (Scanner in = new Scanner(dataFile, "utf-8")) {
			while (in.hasNextLine()) {
				String line = in.nextLine();
				int i;
				
				i = StringUtils.indexOf(" \t", line);
				double target = Double.parseDouble(line.substring(0, i));
				line = line.substring(i + 1);
				
				String qid = "";
				if (line.startsWith("qid:")) {
					i = StringUtils.indexOf(" \t", line);
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
				
				RankInstance instance = new RankInstance(session, session.instances.size(), line);
				session.addExample(instance, target);
			}
		}
		for (Session session : sessions) {
			session.orderByTargetDesc();
		}
	}

	public void dump(File dataFile) throws FileNotFoundException, UnsupportedEncodingException {
		try (PrintWriter out = new PrintWriter(dataFile, "utf-8")) {
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
		}
	}

	public static void main(String args[]) throws FileNotFoundException, UnsupportedEncodingException {
		File fromFile = new File("D:\\SoftwareData\\rhcygwin64\\home\\haimin.shao\\code\\rankboost_code\\w.dat");
		File toFile = new File("D:\\SoftwareData\\rhcygwin64\\home\\haimin.shao\\code\\rankboost_code\\v.dat");
		RankDataset ds = new RankDataset();
		ds.load(fromFile);
		ds.dump(toFile);
	}
}
