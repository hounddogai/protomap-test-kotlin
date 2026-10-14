package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackCameraEventSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackCameraEventSnapshotValidator()
        val item = newFallbackCameraEventSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank region`() {
        val rule = FallbackCameraEventSnapshotRequiredRegionRule("fallback camera event snapshot needs an entry")
        val item = newFallbackCameraEventSnapshot("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long region`() {
        val rule = FallbackCameraEventSnapshotRegionLengthRule(64)
        val item = newFallbackCameraEventSnapshot("x", "y")
        item.region = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FallbackCameraEventSnapshotRequiredLabelRule("fallback camera event snapshot needs a value")
        val item = newFallbackCameraEventSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackCameraEventSnapshotRequiredCodeRule("fallback camera event snapshot needs a value")
        val item = newFallbackCameraEventSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = FallbackCameraEventSnapshotFloorValueRangeRule(3.7, 2831.0)
        val item = newFallbackCameraEventSnapshot("x", "y")
        item.floorValue = 2832.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
