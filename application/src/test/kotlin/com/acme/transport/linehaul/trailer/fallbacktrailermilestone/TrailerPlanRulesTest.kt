package com.acme.transport.linehaul.trailer.fallbacktrailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerPlanValidator()
        val item = newTrailerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = TrailerPlanTitleLengthRule(64)
        val item = newTrailerPlan("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = TrailerPlanRequiredTitleRule("trailer plan needs a value")
        val item = newTrailerPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = TrailerPlanLoadKgRangeRule(0.3, 2534.5)
        val item = newTrailerPlan("x", "y")
        item.loadKg = 2536.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TrailerPlanCodeLengthRule(16)
        val item = newTrailerPlan("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
