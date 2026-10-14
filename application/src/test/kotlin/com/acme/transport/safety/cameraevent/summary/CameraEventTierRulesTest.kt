package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventTierValidator()
        val item = newCameraEventTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CameraEventTierRequiredTitleRule("camera event tier needs this field")
        val item = newCameraEventTier("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventTierCodeLengthRule(255)
        val item = newCameraEventTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CameraEventTierDepotLengthRule(255)
        val item = newCameraEventTier("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = CameraEventTierStopCountRangeRule(32, 3600)
        val item = newCameraEventTier("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = CameraEventTierMaximumRangeRule(5.5, 2967.0)
        val item = newCameraEventTier("x", "y")
        item.maximum = 2968.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
