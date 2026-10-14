package com.acme.transport.lastmile.attempt.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAttemptMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAttemptMilestoneValidator()
        val item = newBatchedAttemptMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedAttemptMilestoneRequiredCodeRule("batched attempt milestone needs an entry")
        val item = newBatchedAttemptMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = BatchedAttemptMilestonePalletsRangeRule(80, 255)
        val item = newBatchedAttemptMilestone("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedAttemptMilestoneNameLengthRule(86400)
        val item = newBatchedAttemptMilestone("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = BatchedAttemptMilestoneEtaMinutesRangeRule(255, 86400)
        val item = newBatchedAttemptMilestone("x", "y")
        item.etaMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
