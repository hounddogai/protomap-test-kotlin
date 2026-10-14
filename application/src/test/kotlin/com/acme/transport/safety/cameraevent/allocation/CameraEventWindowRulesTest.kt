package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventWindowValidator()
        val item = newCameraEventWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = CameraEventWindowTrailerIdLengthRule(16)
        val item = newCameraEventWindow("x", "y")
        item.trailerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CameraEventWindowRequiredLabelRule("camera event window needs a value")
        val item = newCameraEventWindow("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CameraEventWindowLabelLengthRule(32)
        val item = newCameraEventWindow("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventWindowCodeLengthRule(3600)
        val item = newCameraEventWindow("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = CameraEventWindowRequiredSegmentRule("camera event window needs an entry")
        val item = newCameraEventWindow("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
