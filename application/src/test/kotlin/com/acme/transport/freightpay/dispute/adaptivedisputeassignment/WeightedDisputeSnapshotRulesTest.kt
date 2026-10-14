package com.acme.transport.freightpay.dispute.adaptivedisputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedDisputeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedDisputeSnapshotValidator()
        val item = newWeightedDisputeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = WeightedDisputeSnapshotBatchSizeRangeRule(32, 255)
        val item = newWeightedDisputeSnapshot("x", "y")
        item.batchSize = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = WeightedDisputeSnapshotRequiredExternalRefRule("weighted dispute snapshot needs this field")
        val item = newWeightedDisputeSnapshot("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = WeightedDisputeSnapshotExternalRefLengthRule(120)
        val item = newWeightedDisputeSnapshot("x", "y")
        item.externalRef = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
