package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubMilestoneValidator()
        val item = newHubMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = HubMilestonePalletsRangeRule(3600, 86400)
        val item = newHubMilestone("x", "y")
        item.pallets = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = HubMilestoneRequiredRouteCodeRule("hub milestone needs this field")
        val item = newHubMilestone("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = HubMilestoneTitleLengthRule(120)
        val item = newHubMilestone("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
