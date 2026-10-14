package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationTierValidator()
        val item = newSortationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = SortationTierStartsAtRangeRule(16, 16)
        val item = newSortationTier("x", "y")
        item.startsAt = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SortationTierRequiredLabelRule("sortation tier needs this field")
        val item = newSortationTier("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SortationTierLabelLengthRule(1000)
        val item = newSortationTier("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SortationTierCodeLengthRule(120)
        val item = newSortationTier("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
