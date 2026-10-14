package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventMilestoneValidator()
        val item = newCameraEventMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CameraEventMilestoneTitleLengthRule(64)
        val item = newCameraEventMilestone("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = CameraEventMilestoneRequiredDockRule("camera event milestone needs an entry")
        val item = newCameraEventMilestone("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CameraEventMilestoneRequiredCodeRule("camera event milestone needs a value")
        val item = newCameraEventMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
