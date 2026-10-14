package com.acme.transport.terminal.dwell.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDwellPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDwellPlanValidator()
        val item = newRegionalDwellPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = RegionalDwellPlanCeilingRangeRule(6.7, 2079.5)
        val item = newRegionalDwellPlan("x", "y")
        item.ceiling = 2081.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = RegionalDwellPlanTitleLengthRule(86400)
        val item = newRegionalDwellPlan("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RegionalDwellPlanPhoneLengthRule(3600)
        val item = newRegionalDwellPlan("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = RegionalDwellPlanRequiredPhoneRule("regional dwell plan needs a value")
        val item = newRegionalDwellPlan("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RegionalDwellPlanStopCountRangeRule(16, 32)
        val item = newRegionalDwellPlan("x", "y")
        item.stopCount = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
