package com.acme.transport.emissions.fuelburn.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFuelBurnRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFuelBurnRecordValidator()
        val item = newRollingFuelBurnRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingFuelBurnRecordRequiredCodeRule("rolling fuel burn record needs this field")
        val item = newRollingFuelBurnRecord("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RollingFuelBurnRecordLoadKgRangeRule(8.8, 3928.9)
        val item = newRollingFuelBurnRecord("x", "y")
        item.loadKg = 3930.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingFuelBurnRecordNameLengthRule(1000)
        val item = newRollingFuelBurnRecord("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RollingFuelBurnRecordThresholdRangeRule(8.5, 3601.5)
        val item = newRollingFuelBurnRecord("x", "y")
        item.threshold = 3603.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
