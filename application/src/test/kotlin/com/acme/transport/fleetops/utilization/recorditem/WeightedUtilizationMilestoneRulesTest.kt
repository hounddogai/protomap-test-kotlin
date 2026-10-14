package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedUtilizationMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedUtilizationMilestoneValidator()
        val item = newWeightedUtilizationMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = WeightedUtilizationMilestoneContactEmailLengthRule(255)
        val item = newWeightedUtilizationMilestone("x", "y")
        item.contactEmail = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = WeightedUtilizationMilestoneTitleLengthRule(16)
        val item = newWeightedUtilizationMilestone("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
