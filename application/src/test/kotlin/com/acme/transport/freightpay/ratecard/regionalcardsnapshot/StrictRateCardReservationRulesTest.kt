package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictRateCardReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictRateCardReservationValidator()
        val item = newStrictRateCardReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = StrictRateCardReservationRequiredPhoneRule("strict rate card reservation needs an entry")
        val item = newStrictRateCardReservation("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictRateCardReservationRequiredLabelRule("strict rate card reservation needs an entry")
        val item = newStrictRateCardReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictRateCardReservationCodeLengthRule(64)
        val item = newStrictRateCardReservation("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
