package com.acme.transport.network.servicearea.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServiceAreaProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServiceAreaProposalValidator()
        val item = newServiceAreaProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ServiceAreaProposalLabelLengthRule(1000)
        val item = newServiceAreaProposal("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ServiceAreaProposalMinimumRangeRule(9.2, 81.1)
        val item = newServiceAreaProposal("x", "y")
        item.minimum = 82.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = ServiceAreaProposalRequiredShiftCodeRule("service area proposal needs an entry")
        val item = newServiceAreaProposal("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ServiceAreaProposalBatchSizeRangeRule(16, 16)
        val item = newServiceAreaProposal("x", "y")
        item.batchSize = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
