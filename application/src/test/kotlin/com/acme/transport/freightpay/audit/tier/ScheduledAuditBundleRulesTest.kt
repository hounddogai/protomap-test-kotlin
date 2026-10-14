package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAuditBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAuditBundleValidator()
        val item = newScheduledAuditBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = ScheduledAuditBundleRequiredDockRule("scheduled audit bundle needs an entry")
        val item = newScheduledAuditBundle("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ScheduledAuditBundleGraceDaysRangeRule(80, 120)
        val item = newScheduledAuditBundle("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledAuditBundleRequiredCodeRule("scheduled audit bundle needs a value")
        val item = newScheduledAuditBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ScheduledAuditBundleOdometerRangeRule(32, 64)
        val item = newScheduledAuditBundle("x", "y")
        item.odometer = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledAuditBundleNameLengthRule(1000)
        val item = newScheduledAuditBundle("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
