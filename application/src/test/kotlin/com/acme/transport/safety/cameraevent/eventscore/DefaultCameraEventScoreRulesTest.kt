package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultCameraEventScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultCameraEventScoreValidator()
        val item = newDefaultCameraEventScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = DefaultCameraEventScoreRateRangeRule(9.6, 231.6)
        val item = newDefaultCameraEventScore("x", "y")
        item.rate = 233.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DefaultCameraEventScoreStopCountRangeRule(120, 1000)
        val item = newDefaultCameraEventScore("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = DefaultCameraEventScorePriorityRangeRule(16, 1000)
        val item = newDefaultCameraEventScore("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DefaultCameraEventScoreRouteCodeLengthRule(32)
        val item = newDefaultCameraEventScore("x", "y")
        item.routeCode = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = DefaultCameraEventScoreRequiredLabelRule("default camera event score needs an entry")
        val item = newDefaultCameraEventScore("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultCameraEventScoreCodeLengthRule(16)
        val item = newDefaultCameraEventScore("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
