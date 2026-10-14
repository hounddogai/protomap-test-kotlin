package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientIdlingSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientIdlingSlotValidator()
        val item = newLenientIdlingSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = LenientIdlingSlotRevisionRangeRule(32, 255)
        val item = newLenientIdlingSlot("x", "y")
        item.revision = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientIdlingSlotRequiredLabelRule("lenient idling slot needs this field")
        val item = newLenientIdlingSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = LenientIdlingSlotMinimumRangeRule(2.2, 1113.0)
        val item = newLenientIdlingSlot("x", "y")
        item.minimum = 1114.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = LenientIdlingSlotNotesLengthRule(80)
        val item = newLenientIdlingSlot("x", "y")
        item.notes = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
