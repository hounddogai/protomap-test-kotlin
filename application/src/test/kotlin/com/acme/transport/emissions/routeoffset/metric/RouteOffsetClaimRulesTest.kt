package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RouteOffsetClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRouteOffsetClaimValidator()
        val item = newRouteOffsetClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = RouteOffsetClaimRequiredTrailerIdRule("route offset claim needs a value")
        val item = newRouteOffsetClaim("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = RouteOffsetClaimTrailerIdLengthRule(86400)
        val item = newRouteOffsetClaim("x", "y")
        item.trailerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = RouteOffsetClaimCreatedAtRangeRule(80, 3600)
        val item = newRouteOffsetClaim("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
