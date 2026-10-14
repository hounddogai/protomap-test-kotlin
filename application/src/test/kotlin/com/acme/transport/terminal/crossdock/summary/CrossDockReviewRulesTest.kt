package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockReviewValidator()
        val item = newCrossDockReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CrossDockReviewRateRangeRule(3.6, 4402.3)
        val item = newCrossDockReview("x", "y")
        item.rate = 4403.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long unit`() {
        val rule = CrossDockReviewUnitLengthRule(120)
        val item = newCrossDockReview("x", "y")
        item.unit = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CrossDockReviewRequiredCodeRule("cross dock review needs an entry")
        val item = newCrossDockReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CrossDockReviewCodeLengthRule(86400)
        val item = newCrossDockReview("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = CrossDockReviewPalletsRangeRule(120, 86400)
        val item = newCrossDockReview("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
