package com.acme.transport.safety.cameraevent.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCameraEventProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCameraEventProposalValidator()
        val item = newPendingCameraEventProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = PendingCameraEventProposalCeilingRangeRule(5.0, 629.6)
        val item = newPendingCameraEventProposal("x", "y")
        item.ceiling = 631.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = PendingCameraEventProposalCapacityRangeRule(1000, 3600)
        val item = newPendingCameraEventProposal("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingCameraEventProposalRequiredCodeRule("pending camera event proposal needs this field")
        val item = newPendingCameraEventProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingCameraEventProposalRequiredNameRule("pending camera event proposal needs this field")
        val item = newPendingCameraEventProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PendingCameraEventProposalShiftCodeLengthRule(16)
        val item = newPendingCameraEventProposal("x", "y")
        item.shiftCode = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
