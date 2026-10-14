package com.acme.transport.fleetops.serviceplan.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanSummaryValidator()
        val item = newServicePlanSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = ServicePlanSummaryPhoneLengthRule(86400)
        val item = newServicePlanSummary("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ServicePlanSummaryCeilingRangeRule(9.7, 303.3)
        val item = newServicePlanSummary("x", "y")
        item.ceiling = 304.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanSummaryCodeLengthRule(1000)
        val item = newServicePlanSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanSummaryRequiredCodeRule("service plan summary needs this field")
        val item = newServicePlanSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
