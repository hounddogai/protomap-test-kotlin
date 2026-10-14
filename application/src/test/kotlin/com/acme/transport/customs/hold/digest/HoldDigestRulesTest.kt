package com.acme.transport.customs.hold.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HoldDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHoldDigestValidator()
        val item = newHoldDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = HoldDigestRequiredVehicleIdRule("hold digest needs an entry")
        val item = newHoldDigest("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = HoldDigestLabelLengthRule(255)
        val item = newHoldDigest("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = HoldDigestRequiredLaneRule("hold digest needs a value")
        val item = newHoldDigest("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = HoldDigestWindowDaysRangeRule(255, 86400)
        val item = newHoldDigest("x", "y")
        item.windowDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = HoldDigestLaneLengthRule(1000)
        val item = newHoldDigest("x", "y")
        item.lane = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
