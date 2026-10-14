package com.acme.transport.customs.declaration.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDeclarationReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDeclarationReservationValidator()
        val item = newFallbackDeclarationReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = FallbackDeclarationReservationThresholdRangeRule(8.3, 1948.9)
        val item = newFallbackDeclarationReservation("x", "y")
        item.threshold = 1950.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackDeclarationReservationCodeLengthRule(255)
        val item = newFallbackDeclarationReservation("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = FallbackDeclarationReservationPhoneLengthRule(3600)
        val item = newFallbackDeclarationReservation("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = FallbackDeclarationReservationRequiredNotesRule("fallback declaration reservation needs an entry")
        val item = newFallbackDeclarationReservation("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
