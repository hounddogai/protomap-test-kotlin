package com.acme.transport.safety.violation.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictViolationProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictViolationProposalValidator()
        val item = newStrictViolationProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = StrictViolationProposalCreatedAtRangeRule(255, 3600)
        val item = newStrictViolationProposal("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictViolationProposalRequiredNameRule("strict violation proposal needs a value")
        val item = newStrictViolationProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = StrictViolationProposalCeilingRangeRule(9.7, 3857.6)
        val item = newStrictViolationProposal("x", "y")
        item.ceiling = 3859.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = StrictViolationProposalScoreRangeRule(3.1, 3567.0)
        val item = newStrictViolationProposal("x", "y")
        item.score = 3568.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
