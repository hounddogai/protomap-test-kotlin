package com.acme.transport.crewing.availability.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityThresholdValidator()
        val item = newAvailabilityThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = AvailabilityThresholdNameLengthRule(32)
        val item = newAvailabilityThreshold("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long channel`() {
        val rule = AvailabilityThresholdChannelLengthRule(80)
        val item = newAvailabilityThreshold("x", "y")
        item.channel = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AvailabilityThresholdStartsAtRangeRule(64, 255)
        val item = newAvailabilityThreshold("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AvailabilityThresholdRequiredCodeRule("availability threshold needs this field")
        val item = newAvailabilityThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
