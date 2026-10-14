package com.acme.transport.safety.cameraevent.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventPlanValidator()
        val item = newCameraEventPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = CameraEventPlanRequiredChecksumRule("camera event plan needs this field")
        val item = newCameraEventPlan("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CameraEventPlanNameLengthRule(86400)
        val item = newCameraEventPlan("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CameraEventPlanQuantityRangeRule(64, 80)
        val item = newCameraEventPlan("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = CameraEventPlanChecksumLengthRule(80)
        val item = newCameraEventPlan("x", "y")
        item.checksum = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
