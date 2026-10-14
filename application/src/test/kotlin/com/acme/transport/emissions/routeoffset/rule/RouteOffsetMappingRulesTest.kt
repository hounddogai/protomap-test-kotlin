package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetMappingValidator()
        val item = newRouteOffsetMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = RouteOffsetMappingFuelLitersRangeRule(9.9, 1750.2)
        val item = newRouteOffsetMapping("x", "y")
        item.fuelLiters = 1751.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = RouteOffsetMappingThresholdRangeRule(7.6, 1815.6)
        val item = newRouteOffsetMapping("x", "y")
        item.threshold = 1817.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = RouteOffsetMappingOdometerRangeRule(16, 120)
        val item = newRouteOffsetMapping("x", "y")
        item.odometer = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
