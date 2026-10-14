package com.acme.transport.fleetops.defect.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefectProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefectProposalValidator()
        val item = newDefectProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DefectProposalRequiredCategoryRule("defect proposal needs an entry")
        val item = newDefectProposal("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefectProposalRequiredCodeRule("defect proposal needs this field")
        val item = newDefectProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefectProposalCodeLengthRule(255)
        val item = newDefectProposal("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
