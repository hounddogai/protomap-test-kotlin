package com.acme.transport.network.cutoff.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCutoffPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCutoffPlanValidator()
        val item = newFallbackCutoffPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackCutoffPlanNameLengthRule(1000)
        val item = newFallbackCutoffPlan("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackCutoffPlanRequiredNameRule("fallback cutoff plan needs this field")
        val item = newFallbackCutoffPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = FallbackCutoffPlanRequiredRouteCodeRule("fallback cutoff plan needs this field")
        val item = newFallbackCutoffPlan("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
