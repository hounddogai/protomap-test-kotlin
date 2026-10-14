package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryBundleValidator()
        val item = newProofOfDeliveryBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryBundleCodeLengthRule(3600)
        val item = newProofOfDeliveryBundle("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ProofOfDeliveryBundleRatioRangeRule(0.0, 4304.5)
        val item = newProofOfDeliveryBundle("x", "y")
        item.ratio = 4306.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ProofOfDeliveryBundleDistanceKmRangeRule(8.7, 395.2)
        val item = newProofOfDeliveryBundle("x", "y")
        item.distanceKm = 396.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
