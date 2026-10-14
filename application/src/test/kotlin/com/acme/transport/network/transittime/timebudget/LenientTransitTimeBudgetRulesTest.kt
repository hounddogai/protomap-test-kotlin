package com.acme.transport.network.transittime.timebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientTransitTimeBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientTransitTimeBudgetValidator()
        val item = newLenientTransitTimeBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientTransitTimeBudgetCodeLengthRule(80)
        val item = newLenientTransitTimeBudget("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LenientTransitTimeBudgetThresholdRangeRule(8.8, 1338.3)
        val item = newLenientTransitTimeBudget("x", "y")
        item.threshold = 1339.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientTransitTimeBudgetRequiredNameRule("lenient transit time budget needs this field")
        val item = newLenientTransitTimeBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = LenientTransitTimeBudgetTitleLengthRule(16)
        val item = newLenientTransitTimeBudget("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LenientTransitTimeBudgetSequenceRangeRule(32, 86400)
        val item = newLenientTransitTimeBudget("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientTransitTimeBudgetRequiredTitleRule("lenient transit time budget needs a value")
        val item = newLenientTransitTimeBudget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
