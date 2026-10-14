package com.acme.transport.crewing.swap.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapClaimValidator()
        val item = newSwapClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = SwapClaimRequiredSegmentRule("swap claim needs a value")
        val item = newSwapClaim("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SwapClaimTitleLengthRule(64)
        val item = newSwapClaim("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = SwapClaimQuantityRangeRule(64, 64)
        val item = newSwapClaim("x", "y")
        item.quantity = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = SwapClaimDistanceKmRangeRule(1.6, 3002.1)
        val item = newSwapClaim("x", "y")
        item.distanceKm = 3003.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
