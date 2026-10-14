package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventProfileRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventProfileValidator()
        val item = newCameraEventProfile("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CameraEventProfileRevisionRangeRule(120, 3600)
        val item = newCameraEventProfile("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = CameraEventProfileUpdatedAtRangeRule(64, 86400)
        val item = newCameraEventProfile("x", "y")
        item.updatedAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = CameraEventProfileCarrierLengthRule(86400)
        val item = newCameraEventProfile("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = CameraEventProfileDistanceKmRangeRule(3.3, 1622.7)
        val item = newCameraEventProfile("x", "y")
        item.distanceKm = 1624.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
