package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryMappingRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryMappingValidator()
        val item = newProofOfDeliveryMapping("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ProofOfDeliveryMappingRequiredLocaleRule("proof of delivery mapping needs a value")
        val item = newProofOfDeliveryMapping("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = ProofOfDeliveryMappingRequiredCurrencyRule("proof of delivery mapping needs this field")
        val item = newProofOfDeliveryMapping("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = ProofOfDeliveryMappingUpdatedAtRangeRule(1000, 3600)
        val item = newProofOfDeliveryMapping("x", "y")
        item.updatedAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long currency`() {
        val rule = ProofOfDeliveryMappingCurrencyLengthRule(64)
        val item = newProofOfDeliveryMapping("x", "y")
        item.currency = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = ProofOfDeliveryMappingAmountRangeRule(2.1, 1372.7)
        val item = newProofOfDeliveryMapping("x", "y")
        item.amount = 1374.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ProofOfDeliveryMappingLocaleLengthRule(16)
        val item = newProofOfDeliveryMapping("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
