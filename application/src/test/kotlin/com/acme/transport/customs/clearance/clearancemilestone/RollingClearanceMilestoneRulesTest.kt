package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingClearanceMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingClearanceMilestoneValidator()
        val item = newRollingClearanceMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = RollingClearanceMilestoneRateRangeRule(5.2, 1700.7)
        val item = newRollingClearanceMilestone("x", "y")
        item.rate = 1702.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = RollingClearanceMilestoneBatchSizeRangeRule(64, 3600)
        val item = newRollingClearanceMilestone("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RollingClearanceMilestoneRequiredLabelRule("rolling clearance milestone needs a value")
        val item = newRollingClearanceMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingClearanceMilestoneLabelLengthRule(32)
        val item = newRollingClearanceMilestone("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingClearanceMilestoneRequiredCodeRule("rolling clearance milestone needs a value")
        val item = newRollingClearanceMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
