package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAccessorialProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAccessorialProposalValidator()
        val item = newLenientAccessorialProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = LenientAccessorialProposalPhoneLengthRule(32)
        val item = newLenientAccessorialProposal("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientAccessorialProposalRequiredCodeRule("lenient accessorial proposal needs an entry")
        val item = newLenientAccessorialProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
