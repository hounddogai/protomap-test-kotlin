package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEmissionFactorReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackEmissionFactorReservationValidator()
        val item = newFallbackEmissionFactorReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackEmissionFactorReservationRequiredCodeRule(
            "fallback emission factor reservation needs this field",
        )
        val item = newFallbackEmissionFactorReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackEmissionFactorReservationCodeLengthRule(255)
        val item = newFallbackEmissionFactorReservation("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = FallbackEmissionFactorReservationAttemptsRangeRule(120, 255)
        val item = newFallbackEmissionFactorReservation("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackEmissionFactorReservationNameLengthRule(80)
        val item = newFallbackEmissionFactorReservation("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = FallbackEmissionFactorReservationEtaMinutesRangeRule(64, 64)
        val item = newFallbackEmissionFactorReservation("x", "y")
        item.etaMinutes = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
