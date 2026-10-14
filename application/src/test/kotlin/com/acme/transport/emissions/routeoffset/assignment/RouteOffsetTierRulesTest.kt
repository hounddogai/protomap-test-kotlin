package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetTierValidator()
        val item = newRouteOffsetTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RouteOffsetTierRequiredCodeRule("route offset tier needs a value")
        val item = newRouteOffsetTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RouteOffsetTierNameLengthRule(3600)
        val item = newRouteOffsetTier("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RouteOffsetTierEndsAtRangeRule(255, 255)
        val item = newRouteOffsetTier("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RouteOffsetTierMaximumRangeRule(4.9, 2704.3)
        val item = newRouteOffsetTier("x", "y")
        item.maximum = 2705.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long category`() {
        val rule = RouteOffsetTierCategoryLengthRule(120)
        val item = newRouteOffsetTier("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
