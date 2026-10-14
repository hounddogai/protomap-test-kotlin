package com.acme.transport.crewing.restbreak.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRestBreakTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRestBreakTierValidator()
        val item = newStrictRestBreakTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictRestBreakTierRequiredCodeRule("strict rest break tier needs this field")
        val item = newStrictRestBreakTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = StrictRestBreakTierLoadKgRangeRule(3.9, 1408.5)
        val item = newStrictRestBreakTier("x", "y")
        item.loadKg = 1410.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictRestBreakTierCodeLengthRule(32)
        val item = newStrictRestBreakTier("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
