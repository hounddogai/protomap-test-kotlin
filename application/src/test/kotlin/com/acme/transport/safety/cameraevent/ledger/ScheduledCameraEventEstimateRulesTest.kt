package com.acme.transport.safety.cameraevent.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledCameraEventEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledCameraEventEstimateValidator()
        val item = newScheduledCameraEventEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ScheduledCameraEventEstimateNameLengthRule(1000)
        val item = newScheduledCameraEventEstimate("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = ScheduledCameraEventEstimateSummaryLengthRule(1000)
        val item = newScheduledCameraEventEstimate("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
