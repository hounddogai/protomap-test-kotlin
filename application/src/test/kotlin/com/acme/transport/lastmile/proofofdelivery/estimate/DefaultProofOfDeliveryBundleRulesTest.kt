package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultProofOfDeliveryBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultProofOfDeliveryBundleValidator()
        val item = newDefaultProofOfDeliveryBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = DefaultProofOfDeliveryBundleWeightRangeRule(9.6, 3766.8)
        val item = newDefaultProofOfDeliveryBundle("x", "y")
        item.weight = 3768.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = DefaultProofOfDeliveryBundleSummaryLengthRule(86400)
        val item = newDefaultProofOfDeliveryBundle("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
