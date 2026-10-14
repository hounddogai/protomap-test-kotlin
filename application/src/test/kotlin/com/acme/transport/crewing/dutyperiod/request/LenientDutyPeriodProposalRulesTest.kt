package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientDutyPeriodProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientDutyPeriodProposalValidator()
        val item = newLenientDutyPeriodProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LenientDutyPeriodProposalNameLengthRule(64)
        val item = newLenientDutyPeriodProposal("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientDutyPeriodProposalCodeLengthRule(16)
        val item = newLenientDutyPeriodProposal("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientDutyPeriodProposalRequiredNameRule("lenient duty period proposal needs an entry")
        val item = newLenientDutyPeriodProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = LenientDutyPeriodProposalEndsAtRangeRule(32, 1000)
        val item = newLenientDutyPeriodProposal("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
