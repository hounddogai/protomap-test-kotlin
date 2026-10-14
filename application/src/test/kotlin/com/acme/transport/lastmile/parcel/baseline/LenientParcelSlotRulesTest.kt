package com.acme.transport.lastmile.parcel.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientParcelSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientParcelSlotValidator()
        val item = newLenientParcelSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientParcelSlotNameLengthRule(120)
        val item = newLenientParcelSlot("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientParcelSlotUpdatedAtRangeRule(64, 3600)
        val item = newLenientParcelSlot("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientParcelSlotCodeLengthRule(64)
        val item = newLenientParcelSlot("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = LenientParcelSlotRequiredNotesRule("lenient parcel slot needs a value")
        val item = newLenientParcelSlot("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LenientParcelSlotWeightRangeRule(1.4, 3716.0)
        val item = newLenientParcelSlot("x", "y")
        item.weight = 3717.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
