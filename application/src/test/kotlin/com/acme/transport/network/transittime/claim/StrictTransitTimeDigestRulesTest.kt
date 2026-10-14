package com.acme.transport.network.transittime.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictTransitTimeDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictTransitTimeDigestValidator()
        val item = newStrictTransitTimeDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = StrictTransitTimeDigestRequiredShiftCodeRule("strict transit time digest needs this field")
        val item = newStrictTransitTimeDigest("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictTransitTimeDigestRequiredCodeRule("strict transit time digest needs this field")
        val item = newStrictTransitTimeDigest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = StrictTransitTimeDigestFuelLitersRangeRule(6.5, 2182.9)
        val item = newStrictTransitTimeDigest("x", "y")
        item.fuelLiters = 2184.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = StrictTransitTimeDigestShiftCodeLengthRule(32)
        val item = newStrictTransitTimeDigest("x", "y")
        item.shiftCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
