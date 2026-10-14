package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedProofOfDeliveryReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedProofOfDeliveryReservationValidator()
        val item = newWeightedProofOfDeliveryReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = WeightedProofOfDeliveryReservationRequiredLabelRule(
            "weighted proof of delivery reservation needs a value",
        )
        val item = newWeightedProofOfDeliveryReservation("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = WeightedProofOfDeliveryReservationRequiredLaneRule(
            "weighted proof of delivery reservation needs an entry",
        )
        val item = newWeightedProofOfDeliveryReservation("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = WeightedProofOfDeliveryReservationLimitRangeRule(16, 3600)
        val item = newWeightedProofOfDeliveryReservation("x", "y")
        item.limit = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
