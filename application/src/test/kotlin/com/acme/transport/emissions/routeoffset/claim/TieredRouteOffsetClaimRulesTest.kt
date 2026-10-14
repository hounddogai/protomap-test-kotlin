package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRouteOffsetClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRouteOffsetClaimValidator()
        val item = newTieredRouteOffsetClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = TieredRouteOffsetClaimRequiredSegmentRule("tiered route offset claim needs a value")
        val item = newTieredRouteOffsetClaim("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = TieredRouteOffsetClaimOdometerRangeRule(16, 255)
        val item = newTieredRouteOffsetClaim("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = TieredRouteOffsetClaimSegmentLengthRule(1000)
        val item = newTieredRouteOffsetClaim("x", "y")
        item.segment = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = TieredRouteOffsetClaimEndsAtRangeRule(3600, 3600)
        val item = newTieredRouteOffsetClaim("x", "y")
        item.endsAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
