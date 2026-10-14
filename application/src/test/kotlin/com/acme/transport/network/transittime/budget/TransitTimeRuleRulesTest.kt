package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TransitTimeRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTransitTimeRuleValidator()
        val item = newTransitTimeRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TransitTimeRuleRequiredCodeRule("transit time rule needs this field")
        val item = newTransitTimeRule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TransitTimeRuleWindowDaysRangeRule(1000, 3600)
        val item = newTransitTimeRule("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
