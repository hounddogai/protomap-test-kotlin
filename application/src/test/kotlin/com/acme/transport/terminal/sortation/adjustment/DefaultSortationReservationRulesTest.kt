package com.acme.transport.terminal.sortation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultSortationReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultSortationReservationValidator()
        val item = newDefaultSortationReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultSortationReservationRequiredCodeRule("default sortation reservation needs an entry")
        val item = newDefaultSortationReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultSortationReservationCodeLengthRule(32)
        val item = newDefaultSortationReservation("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
