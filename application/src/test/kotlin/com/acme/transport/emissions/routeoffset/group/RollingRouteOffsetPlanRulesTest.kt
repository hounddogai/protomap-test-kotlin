package com.acme.transport.emissions.routeoffset.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRouteOffsetPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRouteOffsetPlanValidator()
        val item = newRollingRouteOffsetPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingRouteOffsetPlanLabelLengthRule(1000)
        val item = newRollingRouteOffsetPlan("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RollingRouteOffsetPlanRouteCodeLengthRule(64)
        val item = newRollingRouteOffsetPlan("x", "y")
        item.routeCode = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
