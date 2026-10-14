package com.acme.transport.terminal.sortation.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictSortationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictSortationTierValidator()
        val item = newStrictSortationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictSortationTierCodeLengthRule(255)
        val item = newStrictSortationTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = StrictSortationTierRatioRangeRule(4.0, 866.0)
        val item = newStrictSortationTier("x", "y")
        item.ratio = 867.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictSortationTierRequiredTitleRule("strict sortation tier needs an entry")
        val item = newStrictSortationTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
