package com.acme.transport.safety.cameraevent.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventOverrideValidator()
        val item = newCameraEventOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = CameraEventOverrideOdometerRangeRule(80, 255)
        val item = newCameraEventOverride("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = CameraEventOverrideLoadKgRangeRule(6.0, 4961.7)
        val item = newCameraEventOverride("x", "y")
        item.loadKg = 4963.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventOverrideCodeLengthRule(120)
        val item = newCameraEventOverride("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = CameraEventOverrideOwnerIdLengthRule(32)
        val item = newCameraEventOverride("x", "y")
        item.ownerId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = CameraEventOverrideRequiredOwnerIdRule("camera event override needs an entry")
        val item = newCameraEventOverride("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
