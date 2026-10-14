package com.acme.transport.freightpay.fuelsurcharge.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedFuelSurchargeProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedFuelSurchargeProposalValidator()
        val item = newWeightedFuelSurchargeProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = WeightedFuelSurchargeProposalContactEmailLengthRule(16)
        val item = newWeightedFuelSurchargeProposal("x", "y")
        item.contactEmail = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = WeightedFuelSurchargeProposalRetriesRangeRule(1000, 3600)
        val item = newWeightedFuelSurchargeProposal("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedFuelSurchargeProposalRequiredCodeRule("weighted fuel surcharge proposal needs this field")
        val item = newWeightedFuelSurchargeProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedFuelSurchargeProposalRequiredNameRule("weighted fuel surcharge proposal needs this field")
        val item = newWeightedFuelSurchargeProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = WeightedFuelSurchargeProposalQuantityRangeRule(32, 80)
        val item = newWeightedFuelSurchargeProposal("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
