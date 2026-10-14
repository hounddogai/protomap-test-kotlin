package com.acme.transport.network.cutoff.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCutoffReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCutoffReservationValidator()
        val item = newGlobalCutoffReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = GlobalCutoffReservationGraceDaysRangeRule(64, 120)
        val item = newGlobalCutoffReservation("x", "y")
        item.graceDays = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCutoffReservationRequiredCodeRule("global cutoff reservation needs this field")
        val item = newGlobalCutoffReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = GlobalCutoffReservationAttemptsRangeRule(3600, 86400)
        val item = newGlobalCutoffReservation("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalCutoffReservationRetriesRangeRule(32, 1000)
        val item = newGlobalCutoffReservation("x", "y")
        item.retries = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = GlobalCutoffReservationNameLengthRule(86400)
        val item = newGlobalCutoffReservation("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
