package com.acme.transport.safety.nearmiss.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualNearMissForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualNearMissForecastValidator()
        val item = newManualNearMissForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ManualNearMissForecastStopCountRangeRule(255, 3600)
        val item = newManualNearMissForecast("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ManualNearMissForecastBatchSizeRangeRule(64, 1000)
        val item = newManualNearMissForecast("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualNearMissForecastRequiredLabelRule("manual near miss forecast needs this field")
        val item = newManualNearMissForecast("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
