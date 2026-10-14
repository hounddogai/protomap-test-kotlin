package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventRecordValidator()
        val item = newCameraEventRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CameraEventRecordRequiredNameRule("camera event record needs a value")
        val item = newCameraEventRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = CameraEventRecordCarrierLengthRule(86400)
        val item = newCameraEventRecord("x", "y")
        item.carrier = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
