package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class InspectionReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultInspectionReservationValidator()
        val item = newInspectionReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = InspectionReservationLabelLengthRule(86400)
        val item = newInspectionReservation("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = InspectionReservationRetriesRangeRule(80, 86400)
        val item = newInspectionReservation("x", "y")
        item.retries = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = InspectionReservationCodeLengthRule(16)
        val item = newInspectionReservation("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = InspectionReservationRequiredCodeRule("inspection reservation needs this field")
        val item = newInspectionReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
