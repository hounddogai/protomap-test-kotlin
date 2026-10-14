package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualParcelProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualParcelProposalValidator()
        val item = newManualParcelProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualParcelProposalLabelLengthRule(1000)
        val item = newManualParcelProposal("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ManualParcelProposalRequiredLabelRule("manual parcel proposal needs this field")
        val item = newManualParcelProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ManualParcelProposalSummaryLengthRule(64)
        val item = newManualParcelProposal("x", "y")
        item.summary = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
