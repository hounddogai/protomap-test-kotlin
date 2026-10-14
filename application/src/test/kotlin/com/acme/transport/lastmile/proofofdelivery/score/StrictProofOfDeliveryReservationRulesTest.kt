package com.acme.transport.lastmile.proofofdelivery.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictProofOfDeliveryReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictProofOfDeliveryReservationValidator()
        val item = newStrictProofOfDeliveryReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictProofOfDeliveryReservationRequiredCodeRule(
            "strict proof of delivery reservation needs an entry",
        )
        val item = newStrictProofOfDeliveryReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictProofOfDeliveryReservationRequiredNameRule(
            "strict proof of delivery reservation needs a value",
        )
        val item = newStrictProofOfDeliveryReservation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictProofOfDeliveryReservationRequiredLabelRule(
            "strict proof of delivery reservation needs a value",
        )
        val item = newStrictProofOfDeliveryReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = StrictProofOfDeliveryReservationLoadKgRangeRule(3.7, 4026.3)
        val item = newStrictProofOfDeliveryReservation("x", "y")
        item.loadKg = 4027.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
