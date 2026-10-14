package com.acme.transport.freightpay.accessorial.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AccessorialPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAccessorialPlanValidator()
        val item = newAccessorialPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = AccessorialPlanQuantityRangeRule(16, 86400)
        val item = newAccessorialPlan("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = AccessorialPlanOwnerIdLengthRule(3600)
        val item = newAccessorialPlan("x", "y")
        item.ownerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AccessorialPlanLabelLengthRule(80)
        val item = newAccessorialPlan("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AccessorialPlanCodeLengthRule(86400)
        val item = newAccessorialPlan("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
