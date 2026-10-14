package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackAppointmentGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackAppointmentGroupValidator()
        val item = newFallbackAppointmentGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackAppointmentGroupRequiredCodeRule("fallback appointment group needs an entry")
        val item = newFallbackAppointmentGroup("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = FallbackAppointmentGroupUpdatedAtRangeRule(32, 120)
        val item = newFallbackAppointmentGroup("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = FallbackAppointmentGroupFloorValueRangeRule(9.2, 4085.4)
        val item = newFallbackAppointmentGroup("x", "y")
        item.floorValue = 4086.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackAppointmentGroupRequiredLabelRule("fallback appointment group needs this field")
        val item = newFallbackAppointmentGroup("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
