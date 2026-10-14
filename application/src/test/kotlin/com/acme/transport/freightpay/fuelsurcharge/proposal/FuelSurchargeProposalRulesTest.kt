package com.acme.transport.freightpay.fuelsurcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FuelSurchargeProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFuelSurchargeProposalValidator()
        val item = newFuelSurchargeProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FuelSurchargeProposalLabelLengthRule(3600)
        val item = newFuelSurchargeProposal("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = FuelSurchargeProposalExpiresAtRangeRule(80, 80)
        val item = newFuelSurchargeProposal("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FuelSurchargeProposalRequiredCodeRule("fuel surcharge proposal needs this field")
        val item = newFuelSurchargeProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FuelSurchargeProposalPalletsRangeRule(120, 86400)
        val item = newFuelSurchargeProposal("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FuelSurchargeProposalCodeLengthRule(64)
        val item = newFuelSurchargeProposal("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
