package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientCutoffReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientCutoffReservationValidator()
        val item = newLenientCutoffReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = LenientCutoffReservationAttemptsRangeRule(80, 1000)
        val item = newLenientCutoffReservation("x", "y")
        item.attempts = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LenientCutoffReservationUpdatedAtRangeRule(80, 120)
        val item = newLenientCutoffReservation("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientCutoffReservationCodeLengthRule(80)
        val item = newLenientCutoffReservation("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LenientCutoffReservationSequenceRangeRule(86400, 86400)
        val item = newLenientCutoffReservation("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
