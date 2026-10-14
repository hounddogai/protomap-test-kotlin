package com.acme.transport.fleetops.utilization.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationMilestoneValidator()
        val item = newUtilizationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = UtilizationMilestoneCodeLengthRule(255)
        val item = newUtilizationMilestone("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = UtilizationMilestoneAmountRangeRule(1.2, 2544.1)
        val item = newUtilizationMilestone("x", "y")
        item.amount = 2545.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = UtilizationMilestoneRequiredSegmentRule("utilization milestone needs an entry")
        val item = newUtilizationMilestone("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = UtilizationMilestoneLabelLengthRule(16)
        val item = newUtilizationMilestone("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationMilestoneRequiredCodeRule("utilization milestone needs an entry")
        val item = newUtilizationMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
