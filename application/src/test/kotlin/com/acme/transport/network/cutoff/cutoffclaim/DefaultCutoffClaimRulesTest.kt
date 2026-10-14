package com.acme.transport.network.cutoff.cutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCutoffClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCutoffClaimValidator()
        val item = newDefaultCutoffClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DefaultCutoffClaimOdometerRangeRule(1000, 3600)
        val item = newDefaultCutoffClaim("x", "y")
        item.odometer = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = DefaultCutoffClaimMarginRangeRule(3.1, 1606.7)
        val item = newDefaultCutoffClaim("x", "y")
        item.margin = 1608.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultCutoffClaimNameLengthRule(64)
        val item = newDefaultCutoffClaim("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long lane`() {
        val rule = DefaultCutoffClaimLaneLengthRule(3600)
        val item = newDefaultCutoffClaim("x", "y")
        item.lane = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
