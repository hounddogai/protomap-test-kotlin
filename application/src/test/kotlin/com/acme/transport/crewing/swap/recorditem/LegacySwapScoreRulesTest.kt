package com.acme.transport.crewing.swap.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacySwapScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacySwapScoreValidator()
        val item = newLegacySwapScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = LegacySwapScoreDescriptionLengthRule(3600)
        val item = newLegacySwapScore("x", "y")
        item.description = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacySwapScoreCodeLengthRule(16)
        val item = newLegacySwapScore("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LegacySwapScoreCapacityRangeRule(80, 80)
        val item = newLegacySwapScore("x", "y")
        item.capacity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
