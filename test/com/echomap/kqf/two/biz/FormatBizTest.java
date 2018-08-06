/**
 * 
 */
package com.echomap.kqf.two.biz;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.echomap.kqf.biz.TextBiz;
import com.echomap.kqf.data.FormatDao;

/**
 * @author mkatz
 * 
 */
public class FormatBizTest {

	@Test
	public void testCenter1a() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "* * * ";
		boolean centerThisLine = TextBiz.centerCheck(dao, st);
		if (centerThisLine) {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1a: Ok! Centerable!");
		} else {
			Assert.fail("testCenter1a: Should be centerable!");
		}
	}

	@Test
	public void testCenter1b() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "And this is what *i say*. ";
		boolean centerThisLine = TextBiz.centerCheck(dao, st);
		if (centerThisLine) {
			Assert.fail("testCenter1b: Should not be centerable!");
		} else {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1b: Ok! NOT Centerable!");
		}
	}

	@Test
	public void testCenter1c() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "**And this is what *i say*. ";
		boolean centerThisLine = TextBiz.centerCheck(dao, st);
		if (centerThisLine) {
			Assert.fail("testCenter1c: Should not be centerable!");
		} else {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1c: Ok! NOT Centerable!");
		}
	}

	@Test
	public void testCenter1d() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "**...";
		boolean centerThisLine = TextBiz.centerCheck(dao, st);
		if (centerThisLine) {
			Assert.fail("testCenter1d: Should not be centerable!");
		} else {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1d: Ok! NOT Centerable!");
		}
	}

	@Test
	public void testCenter1e() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "** ";
		boolean centerThisLine = TextBiz.centerCheck(dao, st);
		if (centerThisLine) {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1e: Ok! Centerable!");
		} else {
			Assert.fail("testCenter1e: Should be centerable!");
		}
	}

	@Test
	public void testCenter2a() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		dao.setCenterableLineText("~~~");
		final String st = "~~~";
		boolean centerThisLine = TextBiz.centerCheck(dao, st);
		if (centerThisLine) {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter2a: Ok! Centerable!");
		} else {
			Assert.fail("testCenter2a: Should be centerable!");
		}
	}

	@Test
	public void testCenter2b() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		dao.setCenterableLineText("~~~");
		final String st = "~~~Pie in the sky~~~";
		boolean centerThisLine = TextBiz.centerCheck(dao, st);
		if (centerThisLine) {
			Assert.fail("testCenter2b: Should not be centerable!");
		} else {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter2b: Ok! NOT Centerable!");
		}
	}

	@Test
	public void testDropCap1a() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "Mary had a little lamb,";
		try {
			final StringBuilder sbuf = new StringBuilder();
			final List<String> slist = TextBiz.doDropCapsList(st, "[[", "]]");
			System.out.println("Slist>>>");
			for (String str : slist) {
				System.out.println(str);
				sbuf.append(str);
			}
			System.out.println("<<<Slist");
			System.out.println("sbuf: '" + sbuf + "'");
			if (sbuf.toString().compareTo("<span class=\"dropcaps\">M</span>ary had a little lamb,") == 0)
				// Assert.fail("testDropCap1a: OK!");
				System.out.println("testDropCap1a: OK!");
			else
				Assert.fail("testDropCap1a: Not match!");
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(" testDropCap1a: " + e.getMessage());
		}
	}

	@Test
	public void testDropCap1b() {
		// final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "\tMary had a little lamb,";
		try {
			final StringBuilder sbuf = new StringBuilder();
			final List<String> slist = TextBiz.doDropCapsList(st, "[[", "]]");
			System.out.println("Slist>>>");
			for (String str : slist) {
				System.out.println(str);
				sbuf.append(str);
			}
			System.out.println("<<<Slist");
			System.out.println("sbuf: '" + sbuf + "'");
			if (sbuf.toString().compareTo("<span class=\"dropcaps\">M</span>ary had a little lamb,") == 0)
				// Assert.fail("testDropCap1a: OK!");
				System.out.println("testDropCap1a: OK!");
			else
				Assert.fail("testDropCap1a: Not match!");
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(" testDropCap1a: " + e.getMessage());
		}
	}
	//
	// @Test
	// public void testDocTags1() {
	// // final FormatBiz biz = new FormatBiz();
	// final FormatDao dao = new FormatDao();
	// dao.setDocTagStart("[[");
	// dao.setDocTagEnd("]]");
	// final String st = "asdf1 [[def: one=two]] asdf2";
	// String testRes = TextBiz.parseForDocTags(st, dao.getDocTagStart(),
	// dao.getDocTagEnd());
	// System.out.println("testRes: '" + testRes + "'");
	// if ("def: one=two".compareTo(testRes) == 0) {
	// System.out.println("testDocTags1: Ok!");
	// } else {
	// Assert.fail("testDocTags1: didn't find proper docTag!");
	// }
	// }
	//
	// @Test
	// public void testDocTags2() {
	// // final FormatBiz biz = new FormatBiz();
	// final FormatDao dao = new FormatDao();
	// dao.setDocTagStart("[[");
	// dao.setDocTagEnd("]]");
	// final String st = "asdf1 [[time: onetwo]] asdf2";
	// String testRes = TextBiz.parseForDocTags(st, dao.getDocTagStart(),
	// dao.getDocTagEnd());
	// System.out.println("testRes: '" + testRes + "'");
	// if ("time: onetwo".compareTo(testRes) == 0) {
	// System.out.println("testDocTags2: Ok!");
	// } else {
	// Assert.fail("testDocTags2: didn't find proper docTag!");
	// }
	// }
}
