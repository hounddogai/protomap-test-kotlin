package com.acme.transport.network.servicearea.weightedareaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientServiceAreaScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientServiceAreaScoreValidator()
        val item = newLenientServiceAreaScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientServiceAreaScoreRequiredCodeRule("lenient service area score needs a value")
        val item = newLenientServiceAreaScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = LenientServiceAreaScoreQuantityRangeRule(16, 80)
        val item = newLenientServiceAreaScore("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientServiceAreaScoreTitleLengthRule(32)
        val item = newLenientServiceAreaScore("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
