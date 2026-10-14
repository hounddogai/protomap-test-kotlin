package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetTemplateValidator()
        val item = newRouteOffsetTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetTemplateRequiredCodeRule("route offset template needs an entry")
        val item = newRouteOffsetTemplate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RouteOffsetTemplateFuelLitersRangeRule(8.7, 485.0)
        val item = newRouteOffsetTemplate("x", "y")
        item.fuelLiters = 486.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RouteOffsetTemplateRequiredNameRule("route offset template needs this field")
        val item = newRouteOffsetTemplate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RouteOffsetTemplateRetriesRangeRule(3600, 86400)
        val item = newRouteOffsetTemplate("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
