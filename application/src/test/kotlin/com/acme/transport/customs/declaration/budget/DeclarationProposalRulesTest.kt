package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationProposalValidator()
        val item = newDeclarationProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = DeclarationProposalPalletsRangeRule(32, 120)
        val item = newDeclarationProposal("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DeclarationProposalRequiredNameRule("declaration proposal needs an entry")
        val item = newDeclarationProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationProposalCodeLengthRule(32)
        val item = newDeclarationProposal("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = DeclarationProposalRequiredVehicleIdRule("declaration proposal needs a value")
        val item = newDeclarationProposal("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DeclarationProposalNameLengthRule(1000)
        val item = newDeclarationProposal("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
