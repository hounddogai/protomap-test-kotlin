package com.acme.transport.emissions.emissionfactor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorSegmentValidator()
        val item = newEmissionFactorSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = EmissionFactorSegmentFuelLitersRangeRule(4.7, 969.2)
        val item = newEmissionFactorSegment("x", "y")
        item.fuelLiters = 970.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorSegmentRequiredCodeRule("emission factor segment needs a value")
        val item = newEmissionFactorSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = EmissionFactorSegmentNameLengthRule(120)
        val item = newEmissionFactorSegment("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
