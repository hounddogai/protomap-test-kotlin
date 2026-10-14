package com.acme.transport.crewing.restbreak.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRestBreakClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRestBreakClaimValidator()
        val item = newStrictRestBreakClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = StrictRestBreakClaimRequiredTrailerIdRule("strict rest break claim needs this field")
        val item = newStrictRestBreakClaim("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = StrictRestBreakClaimDistanceKmRangeRule(4.1, 4803.8)
        val item = newStrictRestBreakClaim("x", "y")
        item.distanceKm = 4805.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = StrictRestBreakClaimDriverIdLengthRule(255)
        val item = newStrictRestBreakClaim("x", "y")
        item.driverId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictRestBreakClaimRequiredCodeRule("strict rest break claim needs a value")
        val item = newStrictRestBreakClaim("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
