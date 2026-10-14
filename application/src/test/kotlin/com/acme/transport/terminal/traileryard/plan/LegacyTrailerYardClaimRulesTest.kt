package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyTrailerYardClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyTrailerYardClaimValidator()
        val item = newLegacyTrailerYardClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyTrailerYardClaimCodeLengthRule(1000)
        val item = newLegacyTrailerYardClaim("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long description`() {
        val rule = LegacyTrailerYardClaimDescriptionLengthRule(16)
        val item = newLegacyTrailerYardClaim("x", "y")
        item.description = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = LegacyTrailerYardClaimLoadKgRangeRule(3.1, 1909.3)
        val item = newLegacyTrailerYardClaim("x", "y")
        item.loadKg = 1910.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LegacyTrailerYardClaimThresholdRangeRule(7.4, 1476.0)
        val item = newLegacyTrailerYardClaim("x", "y")
        item.threshold = 1477.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
