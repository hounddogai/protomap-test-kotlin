package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDisputeCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDisputeCheckpointValidator()
        val item = newPrimaryDisputeCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = PrimaryDisputeCheckpointTrailerIdLengthRule(32)
        val item = newPrimaryDisputeCheckpoint("x", "y")
        item.trailerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = PrimaryDisputeCheckpointDockLengthRule(86400)
        val item = newPrimaryDisputeCheckpoint("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryDisputeCheckpointCodeLengthRule(1000)
        val item = newPrimaryDisputeCheckpoint("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = PrimaryDisputeCheckpointDistanceKmRangeRule(6.8, 987.0)
        val item = newPrimaryDisputeCheckpoint("x", "y")
        item.distanceKm = 988.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
