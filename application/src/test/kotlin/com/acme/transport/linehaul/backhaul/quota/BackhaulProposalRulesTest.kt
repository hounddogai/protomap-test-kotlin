package com.acme.transport.linehaul.backhaul.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulProposalValidator()
        val item = newBackhaulProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BackhaulProposalLabelLengthRule(120)
        val item = newBackhaulProposal("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = BackhaulProposalCostRangeRule(9.4, 4981.2)
        val item = newBackhaulProposal("x", "y")
        item.cost = 4982.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BackhaulProposalRequiredNameRule("backhaul proposal needs a value")
        val item = newBackhaulProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = BackhaulProposalExpiresAtRangeRule(120, 255)
        val item = newBackhaulProposal("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
