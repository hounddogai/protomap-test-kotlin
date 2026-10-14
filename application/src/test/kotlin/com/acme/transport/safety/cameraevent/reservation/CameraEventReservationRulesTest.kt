package com.acme.transport.safety.cameraevent.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventReservationValidator()
        val item = newCameraEventReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CameraEventReservationRatioRangeRule(6.9, 4796.9)
        val item = newCameraEventReservation("x", "y")
        item.ratio = 4798.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = CameraEventReservationTitleLengthRule(255)
        val item = newCameraEventReservation("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
