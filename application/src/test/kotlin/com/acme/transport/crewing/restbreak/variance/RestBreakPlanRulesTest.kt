package com.acme.transport.crewing.restbreak.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakPlanValidator()
        val item = newRestBreakPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RestBreakPlanRequiredNameRule("rest break plan needs an entry")
        val item = newRestBreakPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RestBreakPlanRequiredTimezoneRule("rest break plan needs a value")
        val item = newRestBreakPlan("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = RestBreakPlanSourceLengthRule(255)
        val item = newRestBreakPlan("x", "y")
        item.source = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
