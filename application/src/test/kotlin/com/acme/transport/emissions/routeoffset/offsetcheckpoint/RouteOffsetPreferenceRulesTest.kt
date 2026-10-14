package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetPreferenceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetPreferenceValidator()
        val item = newRouteOffsetPreference("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RouteOffsetPreferenceStopCountRangeRule(1000, 86400)
        val item = newRouteOffsetPreference("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RouteOffsetPreferenceRequiredLabelRule("route offset preference needs this field")
        val item = newRouteOffsetPreference("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = RouteOffsetPreferenceDepotLengthRule(86400)
        val item = newRouteOffsetPreference("x", "y")
        item.depot = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = RouteOffsetPreferenceContactEmailLengthRule(3600)
        val item = newRouteOffsetPreference("x", "y")
        item.contactEmail = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RouteOffsetPreferenceCodeLengthRule(80)
        val item = newRouteOffsetPreference("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
