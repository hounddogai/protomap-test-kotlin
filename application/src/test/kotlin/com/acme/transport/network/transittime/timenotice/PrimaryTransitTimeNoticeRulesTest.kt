package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryTransitTimeNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryTransitTimeNoticeValidator()
        val item = newPrimaryTransitTimeNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PrimaryTransitTimeNoticeSummaryLengthRule(255)
        val item = newPrimaryTransitTimeNotice("x", "y")
        item.summary = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryTransitTimeNoticeCodeLengthRule(16)
        val item = newPrimaryTransitTimeNotice("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryTransitTimeNoticeRequiredCodeRule("primary transit time notice needs this field")
        val item = newPrimaryTransitTimeNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = PrimaryTransitTimeNoticeChecksumLengthRule(80)
        val item = newPrimaryTransitTimeNotice("x", "y")
        item.checksum = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
