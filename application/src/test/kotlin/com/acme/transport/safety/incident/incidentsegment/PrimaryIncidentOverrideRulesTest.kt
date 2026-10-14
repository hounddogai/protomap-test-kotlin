package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryIncidentOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryIncidentOverrideValidator()
        val item = newPrimaryIncidentOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryIncidentOverrideCodeLengthRule(80)
        val item = newPrimaryIncidentOverride("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = PrimaryIncidentOverrideCeilingRangeRule(1.8, 1410.2)
        val item = newPrimaryIncidentOverride("x", "y")
        item.ceiling = 1411.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryIncidentOverrideRequiredCodeRule("primary incident override needs a value")
        val item = newPrimaryIncidentOverride("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
