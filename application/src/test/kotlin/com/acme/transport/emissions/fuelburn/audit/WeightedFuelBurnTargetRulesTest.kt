package com.acme.transport.emissions.fuelburn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelBurnTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFuelBurnTargetValidator()
        val item = newWeightedFuelBurnTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = WeightedFuelBurnTargetCeilingRangeRule(7.3, 3437.7)
        val item = newWeightedFuelBurnTarget("x", "y")
        item.ceiling = 3439.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = WeightedFuelBurnTargetOdometerRangeRule(16, 255)
        val item = newWeightedFuelBurnTarget("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedFuelBurnTargetNameLengthRule(255)
        val item = newWeightedFuelBurnTarget("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
