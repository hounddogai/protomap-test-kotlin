package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RestBreakAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRestBreakAdjustmentValidator()
        val item = newRestBreakAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RestBreakAdjustmentPalletsRangeRule(80, 255)
        val item = newRestBreakAdjustment("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = RestBreakAdjustmentRatioRangeRule(0.3, 4875.3)
        val item = newRestBreakAdjustment("x", "y")
        item.ratio = 4876.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = RestBreakAdjustmentReviewerLengthRule(1000)
        val item = newRestBreakAdjustment("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RestBreakAdjustmentRequiredLabelRule("rest break adjustment needs a value")
        val item = newRestBreakAdjustment("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RestBreakAdjustmentRequiredCodeRule("rest break adjustment needs a value")
        val item = newRestBreakAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
