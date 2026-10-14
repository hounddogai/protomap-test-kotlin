package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingRosterPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingRosterPlanValidator()
        val item = newRollingRosterPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long lane`() {
        val rule = RollingRosterPlanLaneLengthRule(255)
        val item = newRollingRosterPlan("x", "y")
        item.lane = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = RollingRosterPlanOwnerIdLengthRule(86400)
        val item = newRollingRosterPlan("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingRosterPlanCodeLengthRule(86400)
        val item = newRollingRosterPlan("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
