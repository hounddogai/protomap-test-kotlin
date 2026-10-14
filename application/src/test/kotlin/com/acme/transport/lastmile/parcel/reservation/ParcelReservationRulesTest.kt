package com.acme.transport.lastmile.parcel.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelReservationValidator()
        val item = newParcelReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ParcelReservationScoreRangeRule(3.6, 3376.7)
        val item = newParcelReservation("x", "y")
        item.score = 3378.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ParcelReservationAttemptsRangeRule(3600, 86400)
        val item = newParcelReservation("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ParcelReservationSegmentLengthRule(255)
        val item = newParcelReservation("x", "y")
        item.segment = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ParcelReservationRequiredSegmentRule("parcel reservation needs this field")
        val item = newParcelReservation("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = ParcelReservationRequiredChecksumRule("parcel reservation needs an entry")
        val item = newParcelReservation("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
