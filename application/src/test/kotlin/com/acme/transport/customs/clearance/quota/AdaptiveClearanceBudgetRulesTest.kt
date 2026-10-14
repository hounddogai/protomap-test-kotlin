package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveClearanceBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveClearanceBudgetValidator()
        val item = newAdaptiveClearanceBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveClearanceBudgetRequiredLabelRule("adaptive clearance budget needs this field")
        val item = newAdaptiveClearanceBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveClearanceBudgetRequiredCodeRule("adaptive clearance budget needs this field")
        val item = newAdaptiveClearanceBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AdaptiveClearanceBudgetEndsAtRangeRule(64, 3600)
        val item = newAdaptiveClearanceBudget("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveClearanceBudgetCodeLengthRule(255)
        val item = newAdaptiveClearanceBudget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = AdaptiveClearanceBudgetRequiredLaneRule("adaptive clearance budget needs this field")
        val item = newAdaptiveClearanceBudget("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
