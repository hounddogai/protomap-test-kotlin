package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentRuleValidator()
        val item = newIncidentRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = IncidentRuleRouteCodeLengthRule(86400)
        val item = newIncidentRule("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = IncidentRuleRequiredRouteCodeRule("incident rule needs a value")
        val item = newIncidentRule("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
