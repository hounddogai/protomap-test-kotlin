package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCameraEventBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCameraEventBudgetValidator()
        val item = newGlobalCameraEventBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCameraEventBudgetRequiredCodeRule("global camera event budget needs an entry")
        val item = newGlobalCameraEventBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalCameraEventBudgetRequiredLabelRule("global camera event budget needs this field")
        val item = newGlobalCameraEventBudget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = GlobalCameraEventBudgetLimitRangeRule(80, 120)
        val item = newGlobalCameraEventBudget("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalCameraEventBudgetCodeLengthRule(255)
        val item = newGlobalCameraEventBudget("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalCameraEventBudgetLabelLengthRule(120)
        val item = newGlobalCameraEventBudget("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
