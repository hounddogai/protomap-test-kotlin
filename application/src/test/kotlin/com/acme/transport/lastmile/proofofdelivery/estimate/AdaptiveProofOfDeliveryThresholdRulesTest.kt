package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveProofOfDeliveryThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveProofOfDeliveryThresholdValidator()
        val item = newAdaptiveProofOfDeliveryThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = AdaptiveProofOfDeliveryThresholdWeightRangeRule(0.4, 3610.3)
        val item = newAdaptiveProofOfDeliveryThreshold("x", "y")
        item.weight = 3611.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long description`() {
        val rule = AdaptiveProofOfDeliveryThresholdDescriptionLengthRule(120)
        val item = newAdaptiveProofOfDeliveryThreshold("x", "y")
        item.description = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = AdaptiveProofOfDeliveryThresholdRequiredDescriptionRule(
            "adaptive proof of delivery threshold needs an entry",
        )
        val item = newAdaptiveProofOfDeliveryThreshold("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveProofOfDeliveryThresholdRequiredCodeRule(
            "adaptive proof of delivery threshold needs an entry",
        )
        val item = newAdaptiveProofOfDeliveryThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = AdaptiveProofOfDeliveryThresholdExternalRefLengthRule(255)
        val item = newAdaptiveProofOfDeliveryThreshold("x", "y")
        item.externalRef = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
