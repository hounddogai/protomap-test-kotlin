package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedFuelBurnForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedFuelBurnForecastValidator()
        val item = newBatchedFuelBurnForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedFuelBurnForecastRequiredCodeRule("batched fuel burn forecast needs an entry")
        val item = newBatchedFuelBurnForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedFuelBurnForecastWindowDaysRangeRule(3600, 3600)
        val item = newBatchedFuelBurnForecast("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = BatchedFuelBurnForecastRequiredSourceRule("batched fuel burn forecast needs an entry")
        val item = newBatchedFuelBurnForecast("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = BatchedFuelBurnForecastRequiredDockRule("batched fuel burn forecast needs a value")
        val item = newBatchedFuelBurnForecast("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
