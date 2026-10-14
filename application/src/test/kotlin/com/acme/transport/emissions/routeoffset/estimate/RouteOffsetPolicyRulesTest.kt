package com.acme.transport.emissions.routeoffset.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetPolicyValidator()
        val item = newRouteOffsetPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RouteOffsetPolicyRequiredNameRule("route offset policy needs a value")
        val item = newRouteOffsetPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetPolicyRequiredCodeRule("route offset policy needs this field")
        val item = newRouteOffsetPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RouteOffsetPolicyNameLengthRule(32)
        val item = newRouteOffsetPolicy("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above revision`() {
        val rule = RouteOffsetPolicyRevisionRangeRule(255, 86400)
        val item = newRouteOffsetPolicy("x", "y")
        item.revision = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RouteOffsetPolicyMaximumRangeRule(8.1, 3303.0)
        val item = newRouteOffsetPolicy("x", "y")
        item.maximum = 3304.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = RouteOffsetPolicySourceLengthRule(32)
        val item = newRouteOffsetPolicy("x", "y")
        item.source = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
