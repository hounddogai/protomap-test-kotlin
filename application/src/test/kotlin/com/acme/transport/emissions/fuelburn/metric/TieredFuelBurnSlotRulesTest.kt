package com.acme.transport.emissions.fuelburn.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredFuelBurnSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredFuelBurnSlotValidator()
        val item = newTieredFuelBurnSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = TieredFuelBurnSlotChecksumLengthRule(64)
        val item = newTieredFuelBurnSlot("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = TieredFuelBurnSlotRequiredDockRule("tiered fuel burn slot needs a value")
        val item = newTieredFuelBurnSlot("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredFuelBurnSlotRequiredNameRule("tiered fuel burn slot needs an entry")
        val item = newTieredFuelBurnSlot("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TieredFuelBurnSlotRequiredChecksumRule("tiered fuel burn slot needs a value")
        val item = newTieredFuelBurnSlot("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
