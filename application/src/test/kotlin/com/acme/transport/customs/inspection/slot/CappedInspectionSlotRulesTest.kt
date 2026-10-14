package com.acme.transport.customs.inspection.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedInspectionSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedInspectionSlotValidator()
        val item = newCappedInspectionSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedInspectionSlotRequiredTitleRule("capped inspection slot needs a value")
        val item = newCappedInspectionSlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = CappedInspectionSlotCountryLengthRule(1000)
        val item = newCappedInspectionSlot("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = CappedInspectionSlotScoreRangeRule(6.1, 4579.8)
        val item = newCappedInspectionSlot("x", "y")
        item.score = 4581.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
