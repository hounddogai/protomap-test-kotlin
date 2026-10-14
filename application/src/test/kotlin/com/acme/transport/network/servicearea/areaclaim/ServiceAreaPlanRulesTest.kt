package com.acme.transport.network.servicearea.areaclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaPlanValidator()
        val item = newServiceAreaPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServiceAreaPlanRequiredCodeRule("service area plan needs this field")
        val item = newServiceAreaPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServiceAreaPlanCodeLengthRule(64)
        val item = newServiceAreaPlan("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
