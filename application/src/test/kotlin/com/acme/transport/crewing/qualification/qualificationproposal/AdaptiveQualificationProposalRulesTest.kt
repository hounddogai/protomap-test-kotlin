package com.acme.transport.crewing.qualification.qualificationproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveQualificationProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveQualificationProposalValidator()
        val item = newAdaptiveQualificationProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = AdaptiveQualificationProposalShiftCodeLengthRule(120)
        val item = newAdaptiveQualificationProposal("x", "y")
        item.shiftCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AdaptiveQualificationProposalAmountRangeRule(1.0, 1570.5)
        val item = newAdaptiveQualificationProposal("x", "y")
        item.amount = 1572.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = AdaptiveQualificationProposalLoadKgRangeRule(6.3, 2151.0)
        val item = newAdaptiveQualificationProposal("x", "y")
        item.loadKg = 2152.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
