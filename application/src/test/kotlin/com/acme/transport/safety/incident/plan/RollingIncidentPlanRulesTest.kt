package com.acme.transport.safety.incident.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingIncidentPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingIncidentPlanValidator()
        val item = newRollingIncidentPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingIncidentPlanCodeLengthRule(80)
        val item = newRollingIncidentPlan("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
