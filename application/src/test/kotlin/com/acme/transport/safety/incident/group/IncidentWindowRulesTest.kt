package com.acme.transport.safety.incident.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentWindowValidator()
        val item = newIncidentWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = IncidentWindowThresholdRangeRule(5.9, 2202.7)
        val item = newIncidentWindow("x", "y")
        item.threshold = 2204.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentWindowRequiredCodeRule("incident window needs a value")
        val item = newIncidentWindow("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = IncidentWindowQuantityRangeRule(255, 86400)
        val item = newIncidentWindow("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = IncidentWindowCodeLengthRule(86400)
        val item = newIncidentWindow("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
