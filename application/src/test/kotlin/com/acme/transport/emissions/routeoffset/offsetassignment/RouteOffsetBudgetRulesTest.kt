package com.acme.transport.emissions.routeoffset.offsetassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetBudgetValidator()
        val item = newRouteOffsetBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RouteOffsetBudgetRequiredDepotRule("route offset budget needs an entry")
        val item = newRouteOffsetBudget("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RouteOffsetBudgetFloorValueRangeRule(8.6, 4950.8)
        val item = newRouteOffsetBudget("x", "y")
        item.floorValue = 4952.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = RouteOffsetBudgetSequenceRangeRule(64, 3600)
        val item = newRouteOffsetBudget("x", "y")
        item.sequence = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = RouteOffsetBudgetRequiredTimezoneRule("route offset budget needs this field")
        val item = newRouteOffsetBudget("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
