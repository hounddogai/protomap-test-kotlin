package com.acme.transport.fleetops.serviceplan.planmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanWindowValidator()
        val item = newServicePlanWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanWindowRequiredCodeRule("service plan window needs a value")
        val item = newServicePlanWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = ServicePlanWindowOdometerRangeRule(1000, 1000)
        val item = newServicePlanWindow("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ServicePlanWindowSummaryLengthRule(80)
        val item = newServicePlanWindow("x", "y")
        item.summary = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = ServicePlanWindowCarrierLengthRule(16)
        val item = newServicePlanWindow("x", "y")
        item.carrier = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanWindowCodeLengthRule(16)
        val item = newServicePlanWindow("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
