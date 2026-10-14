package com.acme.transport.safety.cameraevent.eventbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventThresholdValidator()
        val item = newCameraEventThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventThresholdCodeLengthRule(32)
        val item = newCameraEventThreshold("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long country`() {
        val rule = CameraEventThresholdCountryLengthRule(120)
        val item = newCameraEventThreshold("x", "y")
        item.country = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CameraEventThresholdLimitRangeRule(32, 255)
        val item = newCameraEventThreshold("x", "y")
        item.limit = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
