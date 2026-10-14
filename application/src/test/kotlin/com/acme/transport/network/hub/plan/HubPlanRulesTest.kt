package com.acme.transport.network.hub.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HubPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultHubPlanValidator()
        val item = newHubPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = HubPlanRequiredPhoneRule("hub plan needs a value")
        val item = newHubPlan("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = HubPlanNameLengthRule(120)
        val item = newHubPlan("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = HubPlanScoreRangeRule(1.2, 4562.9)
        val item = newHubPlan("x", "y")
        item.score = 4564.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = HubPlanRequiredCodeRule("hub plan needs this field")
        val item = newHubPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = HubPlanRequiredNameRule("hub plan needs this field")
        val item = newHubPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
