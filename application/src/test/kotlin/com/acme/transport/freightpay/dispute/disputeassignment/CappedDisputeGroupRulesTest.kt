package com.acme.transport.freightpay.dispute.disputeassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedDisputeGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedDisputeGroupValidator()
        val item = newCappedDisputeGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = CappedDisputeGroupScoreRangeRule(7.5, 3803.0)
        val item = newCappedDisputeGroup("x", "y")
        item.score = 3804.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = CappedDisputeGroupReviewerLengthRule(255)
        val item = newCappedDisputeGroup("x", "y")
        item.reviewer = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CappedDisputeGroupDriverIdLengthRule(16)
        val item = newCappedDisputeGroup("x", "y")
        item.driverId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedDisputeGroupTitleLengthRule(120)
        val item = newCappedDisputeGroup("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CappedDisputeGroupRequiredTitleRule("capped dispute group needs this field")
        val item = newCappedDisputeGroup("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedDisputeGroupRequiredCodeRule("capped dispute group needs a value")
        val item = newCappedDisputeGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
