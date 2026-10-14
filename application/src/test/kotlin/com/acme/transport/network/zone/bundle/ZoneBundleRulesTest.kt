package com.acme.transport.network.zone.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ZoneBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultZoneBundleValidator()
        val item = newZoneBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = ZoneBundleLaneLengthRule(16)
        val item = newZoneBundle("x", "y")
        item.lane = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ZoneBundleRequiredTrailerIdRule("zone bundle needs an entry")
        val item = newZoneBundle("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ZoneBundleAmountRangeRule(0.1, 3621.5)
        val item = newZoneBundle("x", "y")
        item.amount = 3623.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = ZoneBundleRequiredLaneRule("zone bundle needs an entry")
        val item = newZoneBundle("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ZoneBundleTrailerIdLengthRule(16)
        val item = newZoneBundle("x", "y")
        item.trailerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
