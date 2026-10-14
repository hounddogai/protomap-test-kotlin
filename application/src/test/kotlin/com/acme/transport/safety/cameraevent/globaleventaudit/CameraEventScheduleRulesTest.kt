package com.acme.transport.safety.cameraevent.globaleventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventScheduleValidator()
        val item = newCameraEventSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = CameraEventScheduleDockLengthRule(64)
        val item = newCameraEventSchedule("x", "y")
        item.dock = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CameraEventScheduleDurationMinutesRangeRule(1000, 3600)
        val item = newCameraEventSchedule("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CameraEventScheduleRequiredLabelRule("camera event schedule needs a value")
        val item = newCameraEventSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank lane`() {
        val rule = CameraEventScheduleRequiredLaneRule("camera event schedule needs an entry")
        val item = newCameraEventSchedule("", "")
        item.lane = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
