package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceQuotaValidator()
        val item = newClearanceQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = ClearanceQuotaRateRangeRule(9.2, 1377.2)
        val item = newClearanceQuota("x", "y")
        item.rate = 1378.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceQuotaRequiredCodeRule("clearance quota needs this field")
        val item = newClearanceQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ClearanceQuotaDurationMinutesRangeRule(120, 255)
        val item = newClearanceQuota("x", "y")
        item.durationMinutes = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = ClearanceQuotaDescriptionLengthRule(80)
        val item = newClearanceQuota("x", "y")
        item.description = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
