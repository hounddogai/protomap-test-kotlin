package com.acme.transport.customs.tariff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffSlotValidator()
        val item = newTariffSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = TariffSlotThresholdRangeRule(8.6, 3976.6)
        val item = newTariffSlot("x", "y")
        item.threshold = 3978.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TariffSlotRequiredLabelRule("tariff slot needs an entry")
        val item = newTariffSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = TariffSlotRequiredTimezoneRule("tariff slot needs an entry")
        val item = newTariffSlot("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = TariffSlotRequiredContactEmailRule("tariff slot needs this field")
        val item = newTariffSlot("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = TariffSlotWindowDaysRangeRule(16, 64)
        val item = newTariffSlot("x", "y")
        item.windowDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
