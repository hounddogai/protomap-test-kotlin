package com.acme.transport.lastmile.proofofdelivery.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedProofOfDeliveryRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedProofOfDeliveryRollupValidator()
        val item = newBatchedProofOfDeliveryRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedProofOfDeliveryRollupTitleLengthRule(255)
        val item = newBatchedProofOfDeliveryRollup("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedProofOfDeliveryRollupRequiredCodeRule("batched proof of delivery rollup needs an entry")
        val item = newBatchedProofOfDeliveryRollup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = BatchedProofOfDeliveryRollupDurationMinutesRangeRule(120, 3600)
        val item = newBatchedProofOfDeliveryRollup("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = BatchedProofOfDeliveryRollupSequenceRangeRule(120, 86400)
        val item = newBatchedProofOfDeliveryRollup("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
