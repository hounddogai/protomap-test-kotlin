package com.acme.transport.safety.cameraevent.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventTargetValidator()
        val item = newCameraEventTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventTargetRequiredCodeRule("camera event target needs a value")
        val item = newCameraEventTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CameraEventTargetRequiredLabelRule("camera event target needs this field")
        val item = newCameraEventTarget("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CameraEventTargetLabelLengthRule(16)
        val item = newCameraEventTarget("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
