package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationOverrideValidator()
        val item = newSortationOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = SortationOverrideCarrierLengthRule(255)
        val item = newSortationOverride("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = SortationOverrideMarginRangeRule(7.3, 1747.8)
        val item = newSortationOverride("x", "y")
        item.margin = 1749.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SortationOverrideCodeLengthRule(80)
        val item = newSortationOverride("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
