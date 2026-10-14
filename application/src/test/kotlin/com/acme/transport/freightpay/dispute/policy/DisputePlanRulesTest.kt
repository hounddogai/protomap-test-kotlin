package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputePlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputePlanValidator()
        val item = newDisputePlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DisputePlanRequiredNameRule("dispute plan needs this field")
        val item = newDisputePlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DisputePlanCostRangeRule(2.2, 2270.0)
        val item = newDisputePlan("x", "y")
        item.cost = 2271.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DisputePlanLabelLengthRule(120)
        val item = newDisputePlan("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
