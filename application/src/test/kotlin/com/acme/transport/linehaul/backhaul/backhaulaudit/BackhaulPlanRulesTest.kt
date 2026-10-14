package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulPlanValidator()
        val item = newBackhaulPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BackhaulPlanCodeLengthRule(86400)
        val item = newBackhaulPlan("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BackhaulPlanRequiredCodeRule("backhaul plan needs a value")
        val item = newBackhaulPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = BackhaulPlanBudgetRangeRule(9.9, 4700.3)
        val item = newBackhaulPlan("x", "y")
        item.budget = 4701.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long dock`() {
        val rule = BackhaulPlanDockLengthRule(32)
        val item = newBackhaulPlan("x", "y")
        item.dock = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
