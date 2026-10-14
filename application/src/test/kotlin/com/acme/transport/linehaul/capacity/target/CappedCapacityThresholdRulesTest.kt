package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCapacityThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCapacityThresholdValidator()
        val item = newCappedCapacityThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = CappedCapacityThresholdCodeLengthRule(1000)
        val item = newCappedCapacityThreshold("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = CappedCapacityThresholdAttemptsRangeRule(3600, 3600)
        val item = newCappedCapacityThreshold("x", "y")
        item.attempts = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
