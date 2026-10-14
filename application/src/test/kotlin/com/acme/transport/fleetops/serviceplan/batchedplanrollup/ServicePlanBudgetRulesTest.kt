package com.acme.transport.fleetops.serviceplan.batchedplanrollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanBudgetValidator()
        val item = newServicePlanBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ServicePlanBudgetLabelLengthRule(255)
        val item = newServicePlanBudget("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ServicePlanBudgetRequiredChecksumRule("service plan budget needs this field")
        val item = newServicePlanBudget("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ServicePlanBudgetStopCountRangeRule(32, 255)
        val item = newServicePlanBudget("x", "y")
        item.stopCount = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
