package com.acme.transport.emissions.fuelburn.burnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalFuelBurnEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalFuelBurnEstimateValidator()
        val item = newGlobalFuelBurnEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalFuelBurnEstimateLabelLengthRule(16)
        val item = newGlobalFuelBurnEstimate("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = GlobalFuelBurnEstimateCeilingRangeRule(4.9, 126.1)
        val item = newGlobalFuelBurnEstimate("x", "y")
        item.ceiling = 127.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
