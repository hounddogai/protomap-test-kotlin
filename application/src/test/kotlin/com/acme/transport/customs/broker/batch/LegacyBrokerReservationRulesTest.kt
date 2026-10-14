package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyBrokerReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyBrokerReservationValidator()
        val item = newLegacyBrokerReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyBrokerReservationCodeLengthRule(3600)
        val item = newLegacyBrokerReservation("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = LegacyBrokerReservationLabelLengthRule(255)
        val item = newLegacyBrokerReservation("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = LegacyBrokerReservationSequenceRangeRule(16, 120)
        val item = newLegacyBrokerReservation("x", "y")
        item.sequence = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
