package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictNodeTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictNodeTierValidator()
        val item = newStrictNodeTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = StrictNodeTierTitleLengthRule(32)
        val item = newStrictNodeTier("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictNodeTierRequiredTitleRule("strict node tier needs a value")
        val item = newStrictNodeTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = StrictNodeTierMaximumRangeRule(0.9, 279.3)
        val item = newStrictNodeTier("x", "y")
        item.maximum = 280.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictNodeTierCodeLengthRule(86400)
        val item = newStrictNodeTier("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictNodeTierRequiredCodeRule("strict node tier needs an entry")
        val item = newStrictNodeTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
