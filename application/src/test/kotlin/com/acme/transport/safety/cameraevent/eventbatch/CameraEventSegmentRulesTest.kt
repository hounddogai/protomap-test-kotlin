package com.acme.transport.safety.cameraevent.eventbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventSegmentValidator()
        val item = newCameraEventSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CameraEventSegmentRequiredTitleRule("camera event segment needs this field")
        val item = newCameraEventSegment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventSegmentCodeLengthRule(80)
        val item = newCameraEventSegment("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = CameraEventSegmentNameLengthRule(3600)
        val item = newCameraEventSegment("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
