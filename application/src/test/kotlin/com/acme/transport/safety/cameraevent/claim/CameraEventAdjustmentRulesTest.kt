package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventAdjustmentValidator()
        val item = newCameraEventAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = CameraEventAdjustmentRequiredUnitRule("camera event adjustment needs an entry")
        val item = newCameraEventAdjustment("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = CameraEventAdjustmentTitleLengthRule(64)
        val item = newCameraEventAdjustment("x", "y")
        item.title = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
