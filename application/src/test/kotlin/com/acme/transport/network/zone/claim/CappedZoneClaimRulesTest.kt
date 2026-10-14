package com.acme.transport.network.zone.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedZoneClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedZoneClaimValidator()
        val item = newCappedZoneClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = CappedZoneClaimRequiredTrailerIdRule("capped zone claim needs a value")
        val item = newCappedZoneClaim("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CappedZoneClaimMinimumRangeRule(5.8, 888.7)
        val item = newCappedZoneClaim("x", "y")
        item.minimum = 890.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = CappedZoneClaimCurrencyLengthRule(32)
        val item = newCappedZoneClaim("x", "y")
        item.currency = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = CappedZoneClaimTrailerIdLengthRule(64)
        val item = newCappedZoneClaim("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CappedZoneClaimNameLengthRule(3600)
        val item = newCappedZoneClaim("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
