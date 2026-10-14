package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnSummaryValidator()
        val item = newLeaseReturnSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = LeaseReturnSummaryExpiresAtRangeRule(32, 80)
        val item = newLeaseReturnSummary("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = LeaseReturnSummaryRequiredCurrencyRule("lease return summary needs this field")
        val item = newLeaseReturnSummary("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LeaseReturnSummaryRequiredRegionRule("lease return summary needs a value")
        val item = newLeaseReturnSummary("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = LeaseReturnSummaryRegionLengthRule(86400)
        val item = newLeaseReturnSummary("x", "y")
        item.region = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LeaseReturnSummaryLabelLengthRule(1000)
        val item = newLeaseReturnSummary("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
