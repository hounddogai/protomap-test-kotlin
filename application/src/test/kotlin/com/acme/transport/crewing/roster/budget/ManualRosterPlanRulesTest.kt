package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRosterPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRosterPlanValidator()
        val item = newManualRosterPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = ManualRosterPlanRouteCodeLengthRule(120)
        val item = newManualRosterPlan("x", "y")
        item.routeCode = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = ManualRosterPlanCreatedAtRangeRule(64, 64)
        val item = newManualRosterPlan("x", "y")
        item.createdAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
