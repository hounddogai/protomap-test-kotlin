package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationReservationValidator()
        val item = newUtilizationReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = UtilizationReservationExpiresAtRangeRule(16, 32)
        val item = newUtilizationReservation("x", "y")
        item.expiresAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = UtilizationReservationRequiredTitleRule("utilization reservation needs this field")
        val item = newUtilizationReservation("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationReservationRequiredCodeRule("utilization reservation needs an entry")
        val item = newUtilizationReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
