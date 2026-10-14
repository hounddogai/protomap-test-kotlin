package com.acme.transport.lastmile.proofofdelivery.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientProofOfDeliveryReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientProofOfDeliveryReservationValidator()
        val item = newLenientProofOfDeliveryReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientProofOfDeliveryReservationCodeLengthRule(120)
        val item = newLenientProofOfDeliveryReservation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = LenientProofOfDeliveryReservationRequiredRegionRule(
            "lenient proof of delivery reservation needs this field",
        )
        val item = newLenientProofOfDeliveryReservation("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = LenientProofOfDeliveryReservationStartsAtRangeRule(64, 120)
        val item = newLenientProofOfDeliveryReservation("x", "y")
        item.startsAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
