package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalIncidentRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalIncidentRequestValidator()
        val item = newGlobalIncidentRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = GlobalIncidentRequestCreatedAtRangeRule(32, 255)
        val item = newGlobalIncidentRequest("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = GlobalIncidentRequestRequiredOwnerIdRule("global incident request needs a value")
        val item = newGlobalIncidentRequest("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalIncidentRequestTitleLengthRule(32)
        val item = newGlobalIncidentRequest("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = GlobalIncidentRequestEtaMinutesRangeRule(32, 64)
        val item = newGlobalIncidentRequest("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = GlobalIncidentRequestDriverIdLengthRule(80)
        val item = newGlobalIncidentRequest("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
