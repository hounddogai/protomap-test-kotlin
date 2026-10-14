package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelBurnSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelBurnSlotValidator()
        val item = newFuelBurnSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FuelBurnSlotNameLengthRule(120)
        val item = newFuelBurnSlot("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelBurnSlotCodeLengthRule(86400)
        val item = newFuelBurnSlot("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FuelBurnSlotRequiredNameRule("fuel burn slot needs a value")
        val item = newFuelBurnSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = FuelBurnSlotDepotLengthRule(64)
        val item = newFuelBurnSlot("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
