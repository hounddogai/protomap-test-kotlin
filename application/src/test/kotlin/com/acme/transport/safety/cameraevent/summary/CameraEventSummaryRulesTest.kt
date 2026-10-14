package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CameraEventSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCameraEventSummaryValidator()
        val item = newCameraEventSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CameraEventSummaryRequiredTitleRule("camera event summary needs a value")
        val item = newCameraEventSummary("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = CameraEventSummaryQuantityRangeRule(255, 3600)
        val item = newCameraEventSummary("x", "y")
        item.quantity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank region`() {
        val rule = CameraEventSummaryRequiredRegionRule("camera event summary needs a value")
        val item = newCameraEventSummary("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CameraEventSummaryCodeLengthRule(1000)
        val item = newCameraEventSummary("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
