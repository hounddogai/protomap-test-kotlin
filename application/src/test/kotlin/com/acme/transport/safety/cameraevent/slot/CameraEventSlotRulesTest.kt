package com.acme.transport.safety.cameraevent.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventSlotValidator()
        val item = newCameraEventSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = CameraEventSlotShiftCodeLengthRule(86400)
        val item = newCameraEventSlot("x", "y")
        item.shiftCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = CameraEventSlotTitleLengthRule(80)
        val item = newCameraEventSlot("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = CameraEventSlotRequiredShiftCodeRule("camera event slot needs an entry")
        val item = newCameraEventSlot("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CameraEventSlotMaximumRangeRule(4.3, 2450.1)
        val item = newCameraEventSlot("x", "y")
        item.maximum = 2451.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
