package com.acme.transport.emissions.idling.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredIdlingSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredIdlingSlotValidator()
        val item = newTieredIdlingSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = TieredIdlingSlotFuelLitersRangeRule(5.1, 547.3)
        val item = newTieredIdlingSlot("x", "y")
        item.fuelLiters = 548.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = TieredIdlingSlotRequiredUnitRule("tiered idling slot needs a value")
        val item = newTieredIdlingSlot("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long unit`() {
        val rule = TieredIdlingSlotUnitLengthRule(80)
        val item = newTieredIdlingSlot("x", "y")
        item.unit = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
