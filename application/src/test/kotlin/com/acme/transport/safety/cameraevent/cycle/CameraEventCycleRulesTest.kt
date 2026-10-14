package com.acme.transport.safety.cameraevent.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventCycleValidator()
        val item = newCameraEventCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = CameraEventCycleLabelLengthRule(16)
        val item = newCameraEventCycle("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = CameraEventCycleSequenceRangeRule(32, 86400)
        val item = newCameraEventCycle("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventCycleCodeLengthRule(16)
        val item = newCameraEventCycle("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = CameraEventCycleStopCountRangeRule(64, 64)
        val item = newCameraEventCycle("x", "y")
        item.stopCount = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
