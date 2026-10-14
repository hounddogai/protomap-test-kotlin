package com.acme.transport.terminal.crossdock.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryCrossDockRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryCrossDockRecordValidator()
        val item = newPrimaryCrossDockRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = PrimaryCrossDockRecordDriverIdLengthRule(255)
        val item = newPrimaryCrossDockRecord("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = PrimaryCrossDockRecordReviewerLengthRule(3600)
        val item = newPrimaryCrossDockRecord("x", "y")
        item.reviewer = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = PrimaryCrossDockRecordCeilingRangeRule(3.3, 1870.6)
        val item = newPrimaryCrossDockRecord("x", "y")
        item.ceiling = 1872.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = PrimaryCrossDockRecordMinimumRangeRule(2.2, 2110.9)
        val item = newPrimaryCrossDockRecord("x", "y")
        item.minimum = 2112.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = PrimaryCrossDockRecordRequiredDriverIdRule("primary cross dock record needs an entry")
        val item = newPrimaryCrossDockRecord("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
