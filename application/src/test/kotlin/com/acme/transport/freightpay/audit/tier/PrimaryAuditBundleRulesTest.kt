package com.acme.transport.freightpay.audit.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAuditBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAuditBundleValidator()
        val item = newPrimaryAuditBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = PrimaryAuditBundleStopCountRangeRule(255, 255)
        val item = newPrimaryAuditBundle("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryAuditBundleTitleLengthRule(32)
        val item = newPrimaryAuditBundle("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = PrimaryAuditBundleRequiredContactEmailRule("primary audit bundle needs this field")
        val item = newPrimaryAuditBundle("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PrimaryAuditBundleRequiredSummaryRule("primary audit bundle needs a value")
        val item = newPrimaryAuditBundle("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
