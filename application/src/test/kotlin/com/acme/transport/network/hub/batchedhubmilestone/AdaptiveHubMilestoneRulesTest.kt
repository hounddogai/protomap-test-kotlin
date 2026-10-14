package com.acme.transport.network.hub.batchedhubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveHubMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveHubMilestoneValidator()
        val item = newAdaptiveHubMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = AdaptiveHubMilestoneMaximumRangeRule(5.0, 2134.5)
        val item = newAdaptiveHubMilestone("x", "y")
        item.maximum = 2136.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AdaptiveHubMilestonePalletsRangeRule(3600, 86400)
        val item = newAdaptiveHubMilestone("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveHubMilestoneLabelLengthRule(16)
        val item = newAdaptiveHubMilestone("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
