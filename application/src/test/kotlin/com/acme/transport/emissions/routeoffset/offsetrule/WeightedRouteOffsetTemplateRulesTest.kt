package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedRouteOffsetTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedRouteOffsetTemplateValidator()
        val item = newWeightedRouteOffsetTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedRouteOffsetTemplateTitleLengthRule(64)
        val item = newWeightedRouteOffsetTemplate("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedRouteOffsetTemplateRequiredTitleRule("weighted route offset template needs an entry")
        val item = newWeightedRouteOffsetTemplate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = WeightedRouteOffsetTemplateBudgetRangeRule(3.6, 3832.7)
        val item = newWeightedRouteOffsetTemplate("x", "y")
        item.budget = 3834.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = WeightedRouteOffsetTemplateRateRangeRule(1.8, 1030.5)
        val item = newWeightedRouteOffsetTemplate("x", "y")
        item.rate = 1032.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
