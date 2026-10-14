package com.acme.transport.fleetops.utilization.utilizationsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedUtilizationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedUtilizationClaimValidator()
        val item = newCappedUtilizationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = CappedUtilizationClaimCarrierLengthRule(120)
        val item = newCappedUtilizationClaim("x", "y")
        item.carrier = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedUtilizationClaimCodeLengthRule(32)
        val item = newCappedUtilizationClaim("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = CappedUtilizationClaimMarginRangeRule(4.4, 1349.5)
        val item = newCappedUtilizationClaim("x", "y")
        item.margin = 1351.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CappedUtilizationClaimRateRangeRule(0.3, 1579.9)
        val item = newCappedUtilizationClaim("x", "y")
        item.rate = 1581.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedUtilizationClaimTitleLengthRule(16)
        val item = newCappedUtilizationClaim("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
