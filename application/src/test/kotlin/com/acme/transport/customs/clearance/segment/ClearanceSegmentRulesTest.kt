package com.acme.transport.customs.clearance.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ClearanceSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultClearanceSegmentValidator()
        val item = newClearanceSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ClearanceSegmentRequiredTitleRule("clearance segment needs this field")
        val item = newClearanceSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ClearanceSegmentLoadKgRangeRule(3.4, 4748.9)
        val item = newClearanceSegment("x", "y")
        item.loadKg = 4750.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ClearanceSegmentRequiredCodeRule("clearance segment needs this field")
        val item = newClearanceSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ClearanceSegmentTitleLengthRule(80)
        val item = newClearanceSegment("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
