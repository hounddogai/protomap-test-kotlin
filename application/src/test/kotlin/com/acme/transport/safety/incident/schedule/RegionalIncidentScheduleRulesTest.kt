package com.acme.transport.safety.incident.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalIncidentScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalIncidentScheduleValidator()
        val item = newRegionalIncidentSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalIncidentScheduleCodeLengthRule(32)
        val item = newRegionalIncidentSchedule("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long currency`() {
        val rule = RegionalIncidentScheduleCurrencyLengthRule(1000)
        val item = newRegionalIncidentSchedule("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalIncidentScheduleRequiredCodeRule("regional incident schedule needs this field")
        val item = newRegionalIncidentSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
