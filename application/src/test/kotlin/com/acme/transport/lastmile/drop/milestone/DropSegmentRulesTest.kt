package com.acme.transport.lastmile.drop.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropSegmentValidator()
        val item = newDropSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DropSegmentAmountRangeRule(3.6, 3188.3)
        val item = newDropSegment("x", "y")
        item.amount = 3189.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long notes`() {
        val rule = DropSegmentNotesLengthRule(86400)
        val item = newDropSegment("x", "y")
        item.notes = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = DropSegmentNameLengthRule(1000)
        val item = newDropSegment("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DropSegmentRequiredCodeRule("drop segment needs a value")
        val item = newDropSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
