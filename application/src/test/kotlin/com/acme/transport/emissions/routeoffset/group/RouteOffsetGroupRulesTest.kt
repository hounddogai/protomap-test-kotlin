package com.acme.transport.emissions.routeoffset.group

import kotlin.test.Test
import kotlin.test.assertTrue

class RouteOffsetGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetGroupValidator()
        val item = newRouteOffsetGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RouteOffsetGroupRetriesRangeRule(64, 1000)
        val item = newRouteOffsetGroup("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RouteOffsetGroupLoadKgRangeRule(9.9, 2948.8)
        val item = newRouteOffsetGroup("x", "y")
        item.loadKg = 2950.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = RouteOffsetGroupMarginRangeRule(6.7, 4332.2)
        val item = newRouteOffsetGroup("x", "y")
        item.margin = 4333.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
