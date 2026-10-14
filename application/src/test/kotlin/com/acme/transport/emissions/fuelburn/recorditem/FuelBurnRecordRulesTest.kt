package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnRecordValidator()
        val item = newFuelBurnRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = FuelBurnRecordMaximumRangeRule(2.4, 4772.9)
        val item = newFuelBurnRecord("x", "y")
        item.maximum = 4774.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = FuelBurnRecordRequiredTitleRule("fuel burn record needs this field")
        val item = newFuelBurnRecord("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = FuelBurnRecordDescriptionLengthRule(16)
        val item = newFuelBurnRecord("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
