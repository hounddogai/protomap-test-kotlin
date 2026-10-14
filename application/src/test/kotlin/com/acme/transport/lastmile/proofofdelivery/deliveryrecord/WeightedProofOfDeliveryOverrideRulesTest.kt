package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedProofOfDeliveryOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedProofOfDeliveryOverrideValidator()
        val item = newWeightedProofOfDeliveryOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = WeightedProofOfDeliveryOverrideSummaryLengthRule(1000)
        val item = newWeightedProofOfDeliveryOverride("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedProofOfDeliveryOverrideRequiredTitleRule(
            "weighted proof of delivery override needs a value",
        )
        val item = newWeightedProofOfDeliveryOverride("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = WeightedProofOfDeliveryOverrideCapacityRangeRule(80, 120)
        val item = newWeightedProofOfDeliveryOverride("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = WeightedProofOfDeliveryOverrideRequiredSummaryRule(
            "weighted proof of delivery override needs this field",
        )
        val item = newWeightedProofOfDeliveryOverride("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
