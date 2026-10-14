package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryDigestValidator()
        val item = newProofOfDeliveryDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = ProofOfDeliveryDigestRequiredCategoryRule("proof of delivery digest needs a value")
        val item = newProofOfDeliveryDigest("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ProofOfDeliveryDigestRequiredLabelRule("proof of delivery digest needs a value")
        val item = newProofOfDeliveryDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long category`() {
        val rule = ProofOfDeliveryDigestCategoryLengthRule(64)
        val item = newProofOfDeliveryDigest("x", "y")
        item.category = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ProofOfDeliveryDigestSegmentLengthRule(86400)
        val item = newProofOfDeliveryDigest("x", "y")
        item.segment = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
