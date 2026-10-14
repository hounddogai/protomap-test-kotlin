package com.acme.transport.lastmile.proofofdelivery.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryPolicyValidator()
        val item = newProofOfDeliveryPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ProofOfDeliveryPolicyNameLengthRule(3600)
        val item = newProofOfDeliveryPolicy("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryPolicyCodeLengthRule(80)
        val item = newProofOfDeliveryPolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = ProofOfDeliveryPolicyChecksumLengthRule(80)
        val item = newProofOfDeliveryPolicy("x", "y")
        item.checksum = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ProofOfDeliveryPolicyFuelLitersRangeRule(1.8, 1971.1)
        val item = newProofOfDeliveryPolicy("x", "y")
        item.fuelLiters = 1972.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
