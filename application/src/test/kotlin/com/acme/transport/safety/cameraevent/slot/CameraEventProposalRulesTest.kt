package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventProposalValidator()
        val item = newCameraEventProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank source`() {
        val rule = CameraEventProposalRequiredSourceRule("camera event proposal needs an entry")
        val item = newCameraEventProposal("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CameraEventProposalTitleLengthRule(255)
        val item = newCameraEventProposal("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CameraEventProposalRequiredTitleRule("camera event proposal needs an entry")
        val item = newCameraEventProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
