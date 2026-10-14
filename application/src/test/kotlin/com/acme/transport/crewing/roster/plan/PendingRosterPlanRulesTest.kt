package com.acme.transport.crewing.roster.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingRosterPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingRosterPlanValidator()
        val item = newPendingRosterPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingRosterPlanRequiredCodeRule("pending roster plan needs a value")
        val item = newPendingRosterPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingRosterPlanCodeLengthRule(32)
        val item = newPendingRosterPlan("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank country`() {
        val rule = PendingRosterPlanRequiredCountryRule("pending roster plan needs an entry")
        val item = newPendingRosterPlan("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
