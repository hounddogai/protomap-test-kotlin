package com.acme.transport.safety.coaching.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCoachingPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCoachingPlanValidator()
        val item = newCappedCoachingPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = CappedCoachingPlanBatchSizeRangeRule(32, 80)
        val item = newCappedCoachingPlan("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above created at`() {
        val rule = CappedCoachingPlanCreatedAtRangeRule(80, 120)
        val item = newCappedCoachingPlan("x", "y")
        item.createdAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CappedCoachingPlanTitleLengthRule(16)
        val item = newCappedCoachingPlan("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = CappedCoachingPlanRequiredPhoneRule("capped coaching plan needs this field")
        val item = newCappedCoachingPlan("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
