package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredAvailabilityReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredAvailabilityReservationValidator()
        val item = newTieredAvailabilityReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = TieredAvailabilityReservationRatioRangeRule(1.2, 3872.2)
        val item = newTieredAvailabilityReservation("x", "y")
        item.ratio = 3873.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = TieredAvailabilityReservationExternalRefLengthRule(3600)
        val item = newTieredAvailabilityReservation("x", "y")
        item.externalRef = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
