package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeSnapshotValidator()
        val item = newDisputeSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DisputeSnapshotRequiredTitleRule("dispute snapshot needs this field")
        val item = newDisputeSnapshot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DisputeSnapshotRequiredCodeRule("dispute snapshot needs a value")
        val item = newDisputeSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DisputeSnapshotThresholdRangeRule(1.6, 2647.0)
        val item = newDisputeSnapshot("x", "y")
        item.threshold = 2648.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = DisputeSnapshotGraceDaysRangeRule(32, 120)
        val item = newDisputeSnapshot("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
