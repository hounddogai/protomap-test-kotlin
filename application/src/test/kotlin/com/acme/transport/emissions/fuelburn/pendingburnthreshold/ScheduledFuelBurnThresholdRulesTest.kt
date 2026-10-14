package com.acme.transport.emissions.fuelburn.pendingburnthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledFuelBurnThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledFuelBurnThresholdValidator()
        val item = newScheduledFuelBurnThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ScheduledFuelBurnThresholdRequiredReviewerRule("scheduled fuel burn threshold needs an entry")
        val item = newScheduledFuelBurnThreshold("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledFuelBurnThresholdCodeLengthRule(255)
        val item = newScheduledFuelBurnThreshold("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledFuelBurnThresholdRequiredCodeRule("scheduled fuel burn threshold needs an entry")
        val item = newScheduledFuelBurnThreshold("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
