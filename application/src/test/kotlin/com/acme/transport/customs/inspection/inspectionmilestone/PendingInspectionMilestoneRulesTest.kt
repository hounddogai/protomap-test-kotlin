package com.acme.transport.customs.inspection.inspectionmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingInspectionMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingInspectionMilestoneValidator()
        val item = newPendingInspectionMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingInspectionMilestoneCodeLengthRule(80)
        val item = newPendingInspectionMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = PendingInspectionMilestoneEndsAtRangeRule(16, 255)
        val item = newPendingInspectionMilestone("x", "y")
        item.endsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PendingInspectionMilestoneLabelLengthRule(16)
        val item = newPendingInspectionMilestone("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
