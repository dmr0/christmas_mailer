package info.dmr0.christmas_mailer;

import javax.servlet.http.*;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
import java.io.*;

@SuppressWarnings("serial")
public class Christmas_mailerServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/plain; charset=utf-8");
		Calendar now = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
		if ((now.get(Calendar.DATE) != 24) || (now.get(Calendar.MONTH) != Calendar.DECEMBER)) {
			int nowYear = now.get(Calendar.YEAR);
			Calendar christmas = Calendar.getInstance(TimeZone.getTimeZone("Etc/UTC"));
			christmas.set(nowYear, Calendar.DECEMBER, 25, 0, 0, 0);
			if (christmas.getTimeInMillis() < now.getTimeInMillis()) {
				christmas.set(nowYear+1, Calendar.DECEMBER, 25, 0, 0, 0);
			}
			resp.getWriter().println((christmas.getTimeInMillis() - now.getTimeInMillis())/1000 +
				" seconds until Christmas");
			return;
		}

		String mailFrom = "";
		List<String> mailToList = new ArrayList<String>();
		String mailSubj = "";
		String mailBody = "";
		InternetAddress addrFrom = null;
		List<InternetAddress> addrToList = new ArrayList<InternetAddress>();
		try (
			BufferedReader br = new BufferedReader(new InputStreamReader(
			new FileInputStream("WEB-INF/to_recipients.txt"), "UTF-8"));
		) {
			String s;
			while ((s = br.readLine()) != null) {
				if (s.matches(".*\\w\\@\\w.*")) {
					mailToList.add(s);
				}
			}			
		}
		try (
			Scanner sc = new Scanner(
			getServletContext().getResourceAsStream("/WEB-INF/from_subj_body.txt"),
			"UTF-8");
		) {
			sc.useDelimiter("\\A");
			mailFrom = sc.nextLine();
			mailSubj = sc.nextLine();
			mailBody = sc.next();
		}
		StringBuilder debugStr = new StringBuilder();
		debugStr.append("From: =").append(mailFrom).append("=\n");
		for (String s : mailToList) {
			debugStr.append("To: =").append(s).append("=\n");
		}
		debugStr.append("Subj: =").append(mailSubj).append("=\n");
		debugStr.append("Body: =").append(mailBody).append("=\n");
		//resp.getWriter().println(debugStr.toString());
		try {
			addrFrom = new InternetAddress(mailFrom);
			for (String s : mailToList) {
				addrToList.add(new InternetAddress(s));
			}
		} catch (AddressException e) {
			e.printStackTrace(resp.getWriter());
			return;
		}
		debugStr.setLength(0);
		debugStr.append("From name: =").append(addrFrom.getPersonal()).append("=\n");
		debugStr.append("From encoded: =").append(addrFrom.toString()).append("=\n");
		for (InternetAddress ia : addrToList) {
			debugStr.append("To name: =").append(ia.getPersonal()).append("=\n");
			debugStr.append("To encoded: =").append(ia.toString()).append("=\n");
		}
		//resp.getWriter().println(debugStr.toString());
		
		//System.setProperty("mail.mime.charset", "UTF-8");
		Properties props = new Properties();
		Session session = Session.getDefaultInstance(props, null);
		for (InternetAddress addrTo : addrToList) {
			try {
				Message msg = new MimeMessage(session);
				msg.setFrom(addrFrom);
				msg.addRecipient(Message.RecipientType.TO, addrTo);
				msg.setSubject(mailSubj);
				msg.setText(mailBody);
				//ByteArrayOutputStream ba = new ByteArrayOutputStream();
				//msg.writeTo(ba);
				//resp.getWriter().println(ba.toString("UTF-8"));
				Transport.send(msg);
			} catch (MessagingException e) {
				e.printStackTrace(resp.getWriter());
				return;
			}
		}
		resp.getWriter().println("merry Christmas");
	}
}
