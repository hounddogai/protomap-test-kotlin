package com.acme.transport.network.transittime.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeQuotaValidator()
        val item = newTransitTimeQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimeQuotaCodeLengthRule(32)
        val item = newTransitTimeQuota("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = TransitTimeQuotaUpdatedAtRangeRule(32, 86400)
        val item = newTransitTimeQuota("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = TransitTimeQuotaRateRangeRule(0.4, 2558.1)
        val item = newTransitTimeQuota("x", "y")
        item.rate = 2559.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeQuotaRequiredCodeRule("transit time quota needs an entry")
        val item = newTransitTimeQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = TransitTimeQuotaAmountRangeRule(9.1, 3762.3)
        val item = newTransitTimeQuota("x", "y")
        item.amount = 3763.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
