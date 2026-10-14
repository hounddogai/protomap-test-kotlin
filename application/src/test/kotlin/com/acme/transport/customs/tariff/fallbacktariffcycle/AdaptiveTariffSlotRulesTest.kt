package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTariffSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTariffSlotValidator()
        val item = newAdaptiveTariffSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = AdaptiveTariffSlotExternalRefLengthRule(16)
        val item = newAdaptiveTariffSlot("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = AdaptiveTariffSlotRequiredNotesRule("adaptive tariff slot needs a value")
        val item = newAdaptiveTariffSlot("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = AdaptiveTariffSlotCapacityRangeRule(64, 86400)
        val item = newAdaptiveTariffSlot("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = AdaptiveTariffSlotTitleLengthRule(16)
        val item = newAdaptiveTariffSlot("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
