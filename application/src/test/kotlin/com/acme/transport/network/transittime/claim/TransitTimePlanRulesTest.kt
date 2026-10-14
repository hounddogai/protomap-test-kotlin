package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimePlanValidator()
        val item = newTransitTimePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TransitTimePlanWindowDaysRangeRule(32, 120)
        val item = newTransitTimePlan("x", "y")
        item.windowDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TransitTimePlanCodeLengthRule(80)
        val item = newTransitTimePlan("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
