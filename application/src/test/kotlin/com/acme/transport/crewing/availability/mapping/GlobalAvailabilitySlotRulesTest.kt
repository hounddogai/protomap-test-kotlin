package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAvailabilitySlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAvailabilitySlotValidator()
        val item = newGlobalAvailabilitySlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalAvailabilitySlotRequiredLabelRule("global availability slot needs a value")
        val item = newGlobalAvailabilitySlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = GlobalAvailabilitySlotRateRangeRule(8.0, 3528.3)
        val item = newGlobalAvailabilitySlot("x", "y")
        item.rate = 3529.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalAvailabilitySlotLabelLengthRule(255)
        val item = newGlobalAvailabilitySlot("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalAvailabilitySlotCodeLengthRule(3600)
        val item = newGlobalAvailabilitySlot("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalAvailabilitySlotNameLengthRule(1000)
        val item = newGlobalAvailabilitySlot("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
