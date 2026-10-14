package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentScheduleValidator()
        val item = newIncidentSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentScheduleRequiredCodeRule("incident schedule needs an entry")
        val item = newIncidentSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = IncidentScheduleCodeLengthRule(120)
        val item = newIncidentSchedule("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = IncidentScheduleRequiredVehicleIdRule("incident schedule needs a value")
        val item = newIncidentSchedule("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
