/**
 * 
 */
package com.echomap.kqf.two;

import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author mkatz
 * 
 */
public class FormatBizTest {

	@Test
	public void testCenter1a() {
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "* * * ";
		boolean centerThisLine = biz.centerCheck(dao, st);
		if (centerThisLine) {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1a: Ok! Centerable!");
		} else {
			Assert.fail("testCenter1a: Should be centerable!");
		}
	}

	@Test
	public void testCenter1b() {
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "And this is what *i say*. ";
		boolean centerThisLine = biz.centerCheck(dao, st);
		if (centerThisLine) {
			Assert.fail("testCenter1b: Should not be centerable!");
		} else {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1b: Ok! NOT Centerable!");
		}
	}

	@Test
	public void testCenter1c() {
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "**And this is what *i say*. ";
		boolean centerThisLine = biz.centerCheck(dao, st);
		if (centerThisLine) {
			Assert.fail("testCenter1c: Should not be centerable!");
		} else {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1c: Ok! NOT Centerable!");
		}
	}

	@Test
	public void testCenter1d() {
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "**...";
		boolean centerThisLine = biz.centerCheck(dao, st);
		if (centerThisLine) {
			Assert.fail("testCenter1d: Should not be centerable!");
		} else {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1d: Ok! NOT Centerable!");
		}
	}

	@Test
	public void testCenter1e() {
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "** ";
		boolean centerThisLine = biz.centerCheck(dao, st);
		if (centerThisLine) {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter1e: Ok! Centerable!");
		} else {
			Assert.fail("testCenter1e: Should be centerable!");
		}
	}

	@Test
	public void testCenter2a() {
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		dao.setCenterableLineText("~~~");
		final String st = "~~~";
		boolean centerThisLine = biz.centerCheck(dao, st);
		if (centerThisLine) {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter2a: Ok! Centerable!");
		} else {
			Assert.fail("testCenter2a: Should be centerable!");
		}
	}

	@Test
	public void testCenter2b() {
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		dao.setCenterableLineText("~~~");
		final String st = "~~~Pie in the sky~~~";
		boolean centerThisLine = biz.centerCheck(dao, st);
		if (centerThisLine) {
			Assert.fail("testCenter2b: Should not be centerable!");
		} else {
			// Assert.fail("Should not be centerable!");
			System.out.println("testCenter2b: Ok! NOT Centerable!");
		}
	}

	@Test
	public void testDropCap1a() {
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "Mary had a little lamb,";
		try {
			final StringBuilder sbuf = new StringBuilder();
			final List<String> slist = biz.doDropCaps(st);
			System.out.println("Slist>>>");
			for (String str : slist) {
				System.out.println(str);
				sbuf.append(str);
			}
			System.out.println("<<<Slist");
			System.out.println("sbuf: '" + sbuf + "'");
			if (sbuf.toString().compareTo(
					"<span class=\"dropcaps\">M</span>ary had a little lamb,") == 0)
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
		final FormatBiz biz = new FormatBiz();
		final FormatDao dao = new FormatDao();
		dao.setCenterStars(true);
		final String st = "\tMary had a little lamb,";
		try {
			final StringBuilder sbuf = new StringBuilder();
			final List<String> slist = biz.doDropCaps(st);
			System.out.println("Slist>>>");
			for (String str : slist) {
				System.out.println(str);
				sbuf.append(str);
			}
			System.out.println("<<<Slist");
			System.out.println("sbuf: '" + sbuf + "'");
			if (sbuf.toString().compareTo(
					"<span class=\"dropcaps\">M</span>ary had a little lamb,") == 0)
				// Assert.fail("testDropCap1a: OK!");
				System.out.println("testDropCap1a: OK!");
			else
				Assert.fail("testDropCap1a: Not match!");
		} catch (IOException e) {
			e.printStackTrace();
			Assert.fail(" testDropCap1a: " + e.getMessage());
		}
	}
}
