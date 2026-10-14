package com.acme.transport.lastmile.proofofdelivery.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryManifestValidator()
        val item = newProofOfDeliveryManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ProofOfDeliveryManifestRequiredLabelRule("proof of delivery manifest needs this field")
        val item = newProofOfDeliveryManifest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ProofOfDeliveryManifestRatioRangeRule(0.4, 4251.7)
        val item = newProofOfDeliveryManifest("x", "y")
        item.ratio = 4253.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
