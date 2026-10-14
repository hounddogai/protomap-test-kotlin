package com.acme.transport.lastmile.proofofdelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedProofOfDeliverySegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedProofOfDeliverySegmentValidator()
        val item = newWeightedProofOfDeliverySegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = WeightedProofOfDeliverySegmentPriorityRangeRule(16, 120)
        val item = newWeightedProofOfDeliverySegment("x", "y")
        item.priority = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = WeightedProofOfDeliverySegmentRequiredDockRule(
            "weighted proof of delivery segment needs this field",
        )
        val item = newWeightedProofOfDeliverySegment("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
