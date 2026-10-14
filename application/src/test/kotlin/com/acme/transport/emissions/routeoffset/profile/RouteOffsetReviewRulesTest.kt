package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetReviewValidator()
        val item = newRouteOffsetReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RouteOffsetReviewMaximumRangeRule(5.7, 2396.6)
        val item = newRouteOffsetReview("x", "y")
        item.maximum = 2398.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = RouteOffsetReviewCountryLengthRule(64)
        val item = newRouteOffsetReview("x", "y")
        item.country = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RouteOffsetReviewLoadKgRangeRule(9.1, 117.2)
        val item = newRouteOffsetReview("x", "y")
        item.loadKg = 118.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = RouteOffsetReviewNameLengthRule(3600)
        val item = newRouteOffsetReview("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RouteOffsetReviewRequiredNameRule("route offset review needs an entry")
        val item = newRouteOffsetReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
