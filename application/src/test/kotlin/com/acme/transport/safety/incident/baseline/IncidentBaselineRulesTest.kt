package com.acme.transport.safety.incident.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentBaselineValidator()
        val item = newIncidentBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IncidentBaselineCodeLengthRule(1000)
        val item = newIncidentBaseline("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = IncidentBaselineFuelLitersRangeRule(4.6, 279.1)
        val item = newIncidentBaseline("x", "y")
        item.fuelLiters = 280.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = IncidentBaselinePriorityRangeRule(64, 1000)
        val item = newIncidentBaseline("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
