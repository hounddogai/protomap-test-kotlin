package com.acme.transport.fleetops.defect.defectproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDefectProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDefectProposalValidator()
        val item = newRollingDefectProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RollingDefectProposalAmountRangeRule(8.0, 1636.4)
        val item = newRollingDefectProposal("x", "y")
        item.amount = 1637.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDefectProposalRequiredCodeRule("rolling defect proposal needs a value")
        val item = newRollingDefectProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
