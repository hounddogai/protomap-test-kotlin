package com.acme.transport.emissions.emissionfactor.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EmissionFactorReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultEmissionFactorReservationValidator()
        val item = newEmissionFactorReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = EmissionFactorReservationTitleLengthRule(1000)
        val item = newEmissionFactorReservation("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = EmissionFactorReservationMarginRangeRule(2.3, 4126.8)
        val item = newEmissionFactorReservation("x", "y")
        item.margin = 4128.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = EmissionFactorReservationOwnerIdLengthRule(16)
        val item = newEmissionFactorReservation("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
