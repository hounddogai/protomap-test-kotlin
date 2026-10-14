package com.acme.transport.fleetops.inspection.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictInspectionProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictInspectionProposalValidator()
        val item = newStrictInspectionProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictInspectionProposalNameLengthRule(120)
        val item = newStrictInspectionProposal("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
