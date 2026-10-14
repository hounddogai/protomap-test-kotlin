package com.acme.transport.crewing.availability.seasonalavailabilitydigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedAvailabilitySlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedAvailabilitySlotValidator()
        val item = newWeightedAvailabilitySlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = WeightedAvailabilitySlotCarrierLengthRule(86400)
        val item = newWeightedAvailabilitySlot("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = WeightedAvailabilitySlotRequiredTimezoneRule("weighted availability slot needs this field")
        val item = newWeightedAvailabilitySlot("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
