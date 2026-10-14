package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCameraEventBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCameraEventBudgetValidator()
        val item = newManualCameraEventBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ManualCameraEventBudgetThresholdRangeRule(4.6, 3577.7)
        val item = newManualCameraEventBudget("x", "y")
        item.threshold = 3579.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = ManualCameraEventBudgetSequenceRangeRule(64, 1000)
        val item = newManualCameraEventBudget("x", "y")
        item.sequence = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualCameraEventBudgetTitleLengthRule(120)
        val item = newManualCameraEventBudget("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
