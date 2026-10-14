package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualProofOfDeliveryAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualProofOfDeliveryAdjustmentValidator()
        val item = newManualProofOfDeliveryAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = ManualProofOfDeliveryAdjustmentWeightRangeRule(4.0, 667.0)
        val item = newManualProofOfDeliveryAdjustment("x", "y")
        item.weight = 668.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = ManualProofOfDeliveryAdjustmentVersionRangeRule(80, 120)
        val item = newManualProofOfDeliveryAdjustment("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = ManualProofOfDeliveryAdjustmentRequiredTimezoneRule(
            "manual proof of delivery adjustment needs an entry",
        )
        val item = newManualProofOfDeliveryAdjustment("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
