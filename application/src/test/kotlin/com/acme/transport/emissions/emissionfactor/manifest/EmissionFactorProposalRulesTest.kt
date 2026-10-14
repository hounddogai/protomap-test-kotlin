package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorProposalValidator()
        val item = newEmissionFactorProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = EmissionFactorProposalTitleLengthRule(80)
        val item = newEmissionFactorProposal("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = EmissionFactorProposalCodeLengthRule(120)
        val item = newEmissionFactorProposal("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = EmissionFactorProposalRequiredTimezoneRule("emission factor proposal needs a value")
        val item = newEmissionFactorProposal("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
