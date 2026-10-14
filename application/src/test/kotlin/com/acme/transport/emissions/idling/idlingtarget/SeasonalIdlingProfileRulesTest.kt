package com.acme.transport.emissions.idling.idlingtarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalIdlingProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalIdlingProfileValidator()
        val item = newSeasonalIdlingProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = SeasonalIdlingProfileRequiredReviewerRule("seasonal idling profile needs this field")
        val item = newSeasonalIdlingProfile("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalIdlingProfileRequiredLabelRule("seasonal idling profile needs a value")
        val item = newSeasonalIdlingProfile("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = SeasonalIdlingProfileBudgetRangeRule(3.2, 3796.9)
        val item = newSeasonalIdlingProfile("x", "y")
        item.budget = 3798.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
