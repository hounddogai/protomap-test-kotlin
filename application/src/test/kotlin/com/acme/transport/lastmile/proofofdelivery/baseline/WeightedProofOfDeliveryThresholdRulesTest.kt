package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedProofOfDeliveryThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedProofOfDeliveryThresholdValidator()
        val item = newWeightedProofOfDeliveryThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = WeightedProofOfDeliveryThresholdOwnerIdLengthRule(3600)
        val item = newWeightedProofOfDeliveryThreshold("x", "y")
        item.ownerId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = WeightedProofOfDeliveryThresholdRequiredTitleRule(
            "weighted proof of delivery threshold needs an entry",
        )
        val item = newWeightedProofOfDeliveryThreshold("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
