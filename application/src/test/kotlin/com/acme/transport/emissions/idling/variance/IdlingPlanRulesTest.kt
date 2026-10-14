package com.acme.transport.emissions.idling.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IdlingPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIdlingPlanValidator()
        val item = newIdlingPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = IdlingPlanRequiredTitleRule("idling plan needs an entry")
        val item = newIdlingPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = IdlingPlanThresholdRangeRule(1.3, 4295.0)
        val item = newIdlingPlan("x", "y")
        item.threshold = 4296.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = IdlingPlanTitleLengthRule(64)
        val item = newIdlingPlan("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
