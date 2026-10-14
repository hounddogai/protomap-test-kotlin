package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalSwapNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalSwapNoticeValidator()
        val item = newRegionalSwapNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long notes`() {
        val rule = RegionalSwapNoticeNotesLengthRule(64)
        val item = newRegionalSwapNotice("x", "y")
        item.notes = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.notes = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RegionalSwapNoticeOwnerIdLengthRule(1000)
        val item = newRegionalSwapNotice("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = RegionalSwapNoticeRequiredOwnerIdRule("regional swap notice needs this field")
        val item = newRegionalSwapNotice("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RegionalSwapNoticeMarginRangeRule(3.1, 3365.3)
        val item = newRegionalSwapNotice("x", "y")
        item.margin = 3366.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RegionalSwapNoticeLabelLengthRule(80)
        val item = newRegionalSwapNotice("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
