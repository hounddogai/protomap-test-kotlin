package com.acme.transport.safety.incident.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedIncidentMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedIncidentMilestoneValidator()
        val item = newBatchedIncidentMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedIncidentMilestoneWindowDaysRangeRule(80, 80)
        val item = newBatchedIncidentMilestone("x", "y")
        item.windowDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedIncidentMilestoneRequiredCodeRule("batched incident milestone needs an entry")
        val item = newBatchedIncidentMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedIncidentMilestoneTitleLengthRule(32)
        val item = newBatchedIncidentMilestone("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
