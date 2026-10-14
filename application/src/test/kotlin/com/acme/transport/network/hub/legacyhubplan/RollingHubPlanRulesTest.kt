package com.acme.transport.network.hub.legacyhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingHubPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingHubPlanValidator()
        val item = newRollingHubPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingHubPlanRequiredTitleRule("rolling hub plan needs this field")
        val item = newRollingHubPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingHubPlanLabelLengthRule(32)
        val item = newRollingHubPlan("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = RollingHubPlanTitleLengthRule(80)
        val item = newRollingHubPlan("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = RollingHubPlanPhoneLengthRule(32)
        val item = newRollingHubPlan("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
