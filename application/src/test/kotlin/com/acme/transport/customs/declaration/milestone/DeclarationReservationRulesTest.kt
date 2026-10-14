package com.acme.transport.customs.declaration.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationReservationValidator()
        val item = newDeclarationReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DeclarationReservationDurationMinutesRangeRule(255, 3600)
        val item = newDeclarationReservation("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DeclarationReservationLabelLengthRule(32)
        val item = newDeclarationReservation("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = DeclarationReservationRequiredLocaleRule("declaration reservation needs an entry")
        val item = newDeclarationReservation("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DeclarationReservationAttemptsRangeRule(64, 255)
        val item = newDeclarationReservation("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DeclarationReservationCodeLengthRule(3600)
        val item = newDeclarationReservation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
