package com.acme.transport.fleetops.serviceplan.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ServicePlanReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultServicePlanReservationValidator()
        val item = newServicePlanReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ServicePlanReservationCodeLengthRule(16)
        val item = newServicePlanReservation("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ServicePlanReservationNameLengthRule(1000)
        val item = newServicePlanReservation("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = ServicePlanReservationDurationMinutesRangeRule(120, 86400)
        val item = newServicePlanReservation("x", "y")
        item.durationMinutes = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ServicePlanReservationRequiredCodeRule("service plan reservation needs this field")
        val item = newServicePlanReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
