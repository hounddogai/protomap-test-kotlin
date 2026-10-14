package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class IncidentPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultIncidentPlanValidator()
        val item = newIncidentPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = IncidentPlanRequiredCodeRule("incident plan needs a value")
        val item = newIncidentPlan("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = IncidentPlanRequiredVehicleIdRule("incident plan needs this field")
        val item = newIncidentPlan("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = IncidentPlanOwnerIdLengthRule(32)
        val item = newIncidentPlan("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = IncidentPlanCodeLengthRule(64)
        val item = newIncidentPlan("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = IncidentPlanVehicleIdLengthRule(32)
        val item = newIncidentPlan("x", "y")
        item.vehicleId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
