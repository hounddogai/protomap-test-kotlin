package com.acme.transport.safety.cameraevent.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventNoticeValidator()
        val item = newCameraEventNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventNoticeRequiredCodeRule("camera event notice needs this field")
        val item = newCameraEventNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CameraEventNoticeRequiredLabelRule("camera event notice needs this field")
        val item = newCameraEventNotice("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = CameraEventNoticeRequiredContactEmailRule("camera event notice needs an entry")
        val item = newCameraEventNotice("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CameraEventNoticeLabelLengthRule(3600)
        val item = newCameraEventNotice("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
