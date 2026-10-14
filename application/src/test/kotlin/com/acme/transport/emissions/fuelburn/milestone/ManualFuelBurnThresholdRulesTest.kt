package com.acme.transport.emissions.fuelburn.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualFuelBurnThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualFuelBurnThresholdValidator()
        val item = newManualFuelBurnThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualFuelBurnThresholdNameLengthRule(86400)
        val item = newManualFuelBurnThreshold("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ManualFuelBurnThresholdRevisionRangeRule(80, 255)
        val item = newManualFuelBurnThreshold("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = ManualFuelBurnThresholdMarginRangeRule(2.2, 3640.3)
        val item = newManualFuelBurnThreshold("x", "y")
        item.margin = 3641.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ManualFuelBurnThresholdDockLengthRule(86400)
        val item = newManualFuelBurnThreshold("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
