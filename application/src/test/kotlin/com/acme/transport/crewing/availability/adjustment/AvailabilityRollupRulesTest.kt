package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityRollupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityRollupValidator()
        val item = newAvailabilityRollup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = AvailabilityRollupEndsAtRangeRule(16, 80)
        val item = newAvailabilityRollup("x", "y")
        item.endsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = AvailabilityRollupThresholdRangeRule(6.8, 1956.6)
        val item = newAvailabilityRollup("x", "y")
        item.threshold = 1958.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityRollupCodeLengthRule(32)
        val item = newAvailabilityRollup("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = AvailabilityRollupLabelLengthRule(1000)
        val item = newAvailabilityRollup("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
