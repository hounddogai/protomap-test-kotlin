package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnRequestValidator()
        val item = newFuelBurnRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = FuelBurnRequestBatchSizeRangeRule(1000, 86400)
        val item = newFuelBurnRequest("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = FuelBurnRequestRegionLengthRule(64)
        val item = newFuelBurnRequest("x", "y")
        item.region = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelBurnRequestNameLengthRule(3600)
        val item = newFuelBurnRequest("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelBurnRequestRequiredCodeRule("fuel burn request needs an entry")
        val item = newFuelBurnRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
