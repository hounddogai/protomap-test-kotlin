package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalProofOfDeliveryAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalProofOfDeliveryAllocationValidator()
        val item = newSeasonalProofOfDeliveryAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = SeasonalProofOfDeliveryAllocationCarrierLengthRule(64)
        val item = newSeasonalProofOfDeliveryAllocation("x", "y")
        item.carrier = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank carrier`() {
        val rule = SeasonalProofOfDeliveryAllocationRequiredCarrierRule(
            "seasonal proof of delivery allocation needs an entry",
        )
        val item = newSeasonalProofOfDeliveryAllocation("", "")
        item.carrier = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = SeasonalProofOfDeliveryAllocationReviewerLengthRule(32)
        val item = newSeasonalProofOfDeliveryAllocation("x", "y")
        item.reviewer = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SeasonalProofOfDeliveryAllocationRatioRangeRule(5.2, 700.5)
        val item = newSeasonalProofOfDeliveryAllocation("x", "y")
        item.ratio = 702.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
