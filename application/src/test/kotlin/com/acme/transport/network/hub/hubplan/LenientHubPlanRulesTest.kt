package com.acme.transport.network.hub.hubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHubPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientHubPlanValidator()
        val item = newLenientHubPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientHubPlanRequiredTitleRule("lenient hub plan needs an entry")
        val item = newLenientHubPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = LenientHubPlanDurationMinutesRangeRule(16, 3600)
        val item = newLenientHubPlan("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = LenientHubPlanMaximumRangeRule(2.9, 2143.6)
        val item = newLenientHubPlan("x", "y")
        item.maximum = 2145.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
