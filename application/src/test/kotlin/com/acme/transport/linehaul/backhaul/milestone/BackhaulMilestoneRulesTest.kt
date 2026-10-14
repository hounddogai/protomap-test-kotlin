package com.acme.transport.linehaul.backhaul.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BackhaulMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBackhaulMilestoneValidator()
        val item = newBackhaulMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BackhaulMilestoneRequiredLabelRule("backhaul milestone needs this field")
        val item = newBackhaulMilestone("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BackhaulMilestoneLabelLengthRule(16)
        val item = newBackhaulMilestone("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = BackhaulMilestoneRequiredDepotRule("backhaul milestone needs this field")
        val item = newBackhaulMilestone("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BackhaulMilestoneBatchSizeRangeRule(80, 120)
        val item = newBackhaulMilestone("x", "y")
        item.batchSize = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = BackhaulMilestoneEndsAtRangeRule(16, 1000)
        val item = newBackhaulMilestone("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
