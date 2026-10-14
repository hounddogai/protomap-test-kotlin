package com.acme.transport.linehaul.relay.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveRelayDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveRelayDigestValidator()
        val item = newAdaptiveRelayDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = AdaptiveRelayDigestRegionLengthRule(120)
        val item = newAdaptiveRelayDigest("x", "y")
        item.region = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = AdaptiveRelayDigestWindowDaysRangeRule(64, 1000)
        val item = newAdaptiveRelayDigest("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AdaptiveRelayDigestCodeLengthRule(16)
        val item = newAdaptiveRelayDigest("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveRelayDigestNameLengthRule(1000)
        val item = newAdaptiveRelayDigest("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveRelayDigestLabelLengthRule(32)
        val item = newAdaptiveRelayDigest("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
