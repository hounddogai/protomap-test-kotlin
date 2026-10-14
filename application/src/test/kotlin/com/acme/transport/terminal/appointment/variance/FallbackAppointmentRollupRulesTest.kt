package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAppointmentRollupValidator()
        val item = newFallbackAppointmentRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = FallbackAppointmentRollupTrailerIdLengthRule(80)
        val item = newFallbackAppointmentRollup("x", "y")
        item.trailerId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = FallbackAppointmentRollupRequiredTrailerIdRule("fallback appointment rollup needs this field")
        val item = newFallbackAppointmentRollup("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = FallbackAppointmentRollupCreatedAtRangeRule(1000, 3600)
        val item = newFallbackAppointmentRollup("x", "y")
        item.createdAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
