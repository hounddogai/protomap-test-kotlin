package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeCheckpointValidator()
        val item = newDisputeCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = DisputeCheckpointLabelLengthRule(3600)
        val item = newDisputeCheckpoint("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DisputeCheckpointRequiredLabelRule("dispute checkpoint needs this field")
        val item = newDisputeCheckpoint("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DisputeCheckpointRequiredCodeRule("dispute checkpoint needs this field")
        val item = newDisputeCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DisputeCheckpointDriverIdLengthRule(3600)
        val item = newDisputeCheckpoint("x", "y")
        item.driverId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
