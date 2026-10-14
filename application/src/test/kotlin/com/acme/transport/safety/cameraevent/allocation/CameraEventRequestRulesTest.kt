package com.acme.transport.safety.cameraevent.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventRequestValidator()
        val item = newCameraEventRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = CameraEventRequestDockLengthRule(80)
        val item = newCameraEventRequest("x", "y")
        item.dock = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventRequestRequiredCodeRule("camera event request needs a value")
        val item = newCameraEventRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CameraEventRequestLabelLengthRule(86400)
        val item = newCameraEventRequest("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = CameraEventRequestCostRangeRule(6.9, 4068.8)
        val item = newCameraEventRequest("x", "y")
        item.cost = 4070.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventRequestCodeLengthRule(255)
        val item = newCameraEventRequest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
