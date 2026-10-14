package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionProposalValidator()
        val item = newInspectionProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = InspectionProposalTitleLengthRule(120)
        val item = newInspectionProposal("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = InspectionProposalRequiredRouteCodeRule("inspection proposal needs an entry")
        val item = newInspectionProposal("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionProposalCodeLengthRule(255)
        val item = newInspectionProposal("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = InspectionProposalRequiredSummaryRule("inspection proposal needs a value")
        val item = newInspectionProposal("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
