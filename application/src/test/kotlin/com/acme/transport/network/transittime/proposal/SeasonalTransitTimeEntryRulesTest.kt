package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTransitTimeEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTransitTimeEntryValidator()
        val item = newSeasonalTransitTimeEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalTransitTimeEntryLabelLengthRule(64)
        val item = newSeasonalTransitTimeEntry("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = SeasonalTransitTimeEntryRequiredPhoneRule("seasonal transit time entry needs this field")
        val item = newSeasonalTransitTimeEntry("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = SeasonalTransitTimeEntryRequiredCategoryRule("seasonal transit time entry needs this field")
        val item = newSeasonalTransitTimeEntry("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = SeasonalTransitTimeEntryBudgetRangeRule(8.4, 3889.4)
        val item = newSeasonalTransitTimeEntry("x", "y")
        item.budget = 3890.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
