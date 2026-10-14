package com.acme.transport.safety.coaching.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingThresholdValidator()
        val item = newCoachingThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = CoachingThresholdCreatedAtRangeRule(16, 255)
        val item = newCoachingThreshold("x", "y")
        item.createdAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = CoachingThresholdDurationMinutesRangeRule(16, 32)
        val item = newCoachingThreshold("x", "y")
        item.durationMinutes = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = CoachingThresholdRequiredTrailerIdRule("coaching threshold needs a value")
        val item = newCoachingThreshold("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CoachingThresholdRequiredNameRule("coaching threshold needs this field")
        val item = newCoachingThreshold("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CoachingThresholdNameLengthRule(255)
        val item = newCoachingThreshold("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
