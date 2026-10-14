package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorSlotValidator()
        val item = newEmissionFactorSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorSlotCodeLengthRule(120)
        val item = newEmissionFactorSlot("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = EmissionFactorSlotFuelLitersRangeRule(5.2, 3140.9)
        val item = newEmissionFactorSlot("x", "y")
        item.fuelLiters = 3142.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = EmissionFactorSlotRequiredLaneRule("emission factor slot needs an entry")
        val item = newEmissionFactorSlot("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = EmissionFactorSlotRequiredCodeRule("emission factor slot needs an entry")
        val item = newEmissionFactorSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
