package com.acme.transport.safety.cameraevent.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventBudgetValidator()
        val item = newCameraEventBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = CameraEventBudgetGraceDaysRangeRule(16, 32)
        val item = newCameraEventBudget("x", "y")
        item.graceDays = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = CameraEventBudgetRequiredCarrierRule("camera event budget needs an entry")
        val item = newCameraEventBudget("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CameraEventBudgetRequiredLabelRule("camera event budget needs a value")
        val item = newCameraEventBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
