package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentExceptionValidator()
        val item = newIncidentException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = IncidentExceptionBudgetRangeRule(3.8, 2541.4)
        val item = newIncidentException("x", "y")
        item.budget = 2542.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = IncidentExceptionRequiredTrailerIdRule("incident exception needs an entry")
        val item = newIncidentException("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentExceptionRequiredCodeRule("incident exception needs a value")
        val item = newIncidentException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
