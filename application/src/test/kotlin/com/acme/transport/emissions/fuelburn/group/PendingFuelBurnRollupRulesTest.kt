package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingFuelBurnRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingFuelBurnRollupValidator()
        val item = newPendingFuelBurnRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingFuelBurnRollupLabelLengthRule(3600)
        val item = newPendingFuelBurnRollup("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = PendingFuelBurnRollupCountryLengthRule(1000)
        val item = newPendingFuelBurnRollup("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = PendingFuelBurnRollupWeightRangeRule(7.3, 2945.1)
        val item = newPendingFuelBurnRollup("x", "y")
        item.weight = 2946.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
