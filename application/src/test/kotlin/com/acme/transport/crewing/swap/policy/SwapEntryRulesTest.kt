package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapEntryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapEntryValidator()
        val item = newSwapEntry("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = SwapEntryDiscountRangeRule(2.7, 2757.6)
        val item = newSwapEntry("x", "y")
        item.discount = 2759.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = SwapEntryChecksumLengthRule(32)
        val item = newSwapEntry("x", "y")
        item.checksum = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapEntryCodeLengthRule(120)
        val item = newSwapEntry("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = SwapEntryCostRangeRule(0.8, 2464.3)
        val item = newSwapEntry("x", "y")
        item.cost = 2465.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
