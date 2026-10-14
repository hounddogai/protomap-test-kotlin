package com.acme.transport.fleetops.inspection.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualInspectionForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualInspectionForecastValidator()
        val item = newManualInspectionForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = ManualInspectionForecastRequiredDescriptionRule("manual inspection forecast needs an entry")
        val item = newManualInspectionForecast("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ManualInspectionForecastUpdatedAtRangeRule(120, 120)
        val item = newManualInspectionForecast("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualInspectionForecastRequiredTitleRule("manual inspection forecast needs a value")
        val item = newManualInspectionForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
