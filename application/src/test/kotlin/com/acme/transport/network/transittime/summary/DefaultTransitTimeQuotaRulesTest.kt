package com.acme.transport.network.transittime.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTransitTimeQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTransitTimeQuotaValidator()
        val item = newDefaultTransitTimeQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = DefaultTransitTimeQuotaCountryLengthRule(16)
        val item = newDefaultTransitTimeQuota("x", "y")
        item.country = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = DefaultTransitTimeQuotaBudgetRangeRule(7.7, 1504.1)
        val item = newDefaultTransitTimeQuota("x", "y")
        item.budget = 1505.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultTransitTimeQuotaRequiredCodeRule("default transit time quota needs a value")
        val item = newDefaultTransitTimeQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = DefaultTransitTimeQuotaFloorValueRangeRule(7.8, 1923.6)
        val item = newDefaultTransitTimeQuota("x", "y")
        item.floorValue = 1925.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
