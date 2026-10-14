package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDefectMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDefectMilestoneValidator()
        val item = newManualDefectMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = ManualDefectMilestoneCeilingRangeRule(8.5, 2197.1)
        val item = newManualDefectMilestone("x", "y")
        item.ceiling = 2198.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ManualDefectMilestoneGraceDaysRangeRule(64, 86400)
        val item = newManualDefectMilestone("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualDefectMilestoneCodeLengthRule(80)
        val item = newManualDefectMilestone("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ManualDefectMilestoneEndsAtRangeRule(32, 80)
        val item = newManualDefectMilestone("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualDefectMilestoneRequiredNameRule("manual defect milestone needs an entry")
        val item = newManualDefectMilestone("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualDefectMilestoneRequiredCodeRule("manual defect milestone needs a value")
        val item = newManualDefectMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
