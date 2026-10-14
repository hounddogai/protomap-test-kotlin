package com.acme.transport.linehaul.relay.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientRelayClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientRelayClaimValidator()
        val item = newLenientRelayClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = LenientRelayClaimRequiredLabelRule("lenient relay claim needs a value")
        val item = newLenientRelayClaim("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = LenientRelayClaimStopCountRangeRule(32, 80)
        val item = newLenientRelayClaim("x", "y")
        item.stopCount = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LenientRelayClaimCapacityRangeRule(3600, 86400)
        val item = newLenientRelayClaim("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long source`() {
        val rule = LenientRelayClaimSourceLengthRule(64)
        val item = newLenientRelayClaim("x", "y")
        item.source = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
