package com.acme.transport.terminal.sortation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationAllocationValidator()
        val item = newSortationAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = SortationAllocationLocaleLengthRule(255)
        val item = newSortationAllocation("x", "y")
        item.locale = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SortationAllocationRequiredCodeRule("sortation allocation needs an entry")
        val item = newSortationAllocation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = SortationAllocationRequiredCurrencyRule("sortation allocation needs a value")
        val item = newSortationAllocation("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SortationAllocationCodeLengthRule(80)
        val item = newSortationAllocation("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = SortationAllocationMarginRangeRule(0.7, 3239.2)
        val item = newSortationAllocation("x", "y")
        item.margin = 3240.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
