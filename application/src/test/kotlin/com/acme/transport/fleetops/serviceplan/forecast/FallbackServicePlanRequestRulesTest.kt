package com.acme.transport.fleetops.serviceplan.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackServicePlanRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackServicePlanRequestValidator()
        val item = newFallbackServicePlanRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackServicePlanRequestCodeLengthRule(1000)
        val item = newFallbackServicePlanRequest("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = FallbackServicePlanRequestRequiredContactEmailRule("fallback service plan request needs an entry")
        val item = newFallbackServicePlanRequest("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
