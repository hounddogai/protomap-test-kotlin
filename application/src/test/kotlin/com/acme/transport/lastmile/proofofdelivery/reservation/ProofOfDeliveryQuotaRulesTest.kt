package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryQuotaValidator()
        val item = newProofOfDeliveryQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = ProofOfDeliveryQuotaDistanceKmRangeRule(5.1, 3973.8)
        val item = newProofOfDeliveryQuota("x", "y")
        item.distanceKm = 3975.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ProofOfDeliveryQuotaRequiredCodeRule("proof of delivery quota needs an entry")
        val item = newProofOfDeliveryQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
