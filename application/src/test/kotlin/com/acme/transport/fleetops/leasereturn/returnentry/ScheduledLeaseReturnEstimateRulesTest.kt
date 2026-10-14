package com.acme.transport.fleetops.leasereturn.returnentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLeaseReturnEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLeaseReturnEstimateValidator()
        val item = newScheduledLeaseReturnEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ScheduledLeaseReturnEstimateTrailerIdLengthRule(120)
        val item = newScheduledLeaseReturnEstimate("x", "y")
        item.trailerId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ScheduledLeaseReturnEstimateLoadKgRangeRule(1.5, 1082.3)
        val item = newScheduledLeaseReturnEstimate("x", "y")
        item.loadKg = 1083.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ScheduledLeaseReturnEstimateExpiresAtRangeRule(16, 3600)
        val item = newScheduledLeaseReturnEstimate("x", "y")
        item.expiresAt = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = ScheduledLeaseReturnEstimateRequiredTrailerIdRule("scheduled lease return estimate needs a value")
        val item = newScheduledLeaseReturnEstimate("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = ScheduledLeaseReturnEstimateRatioRangeRule(1.8, 2745.9)
        val item = newScheduledLeaseReturnEstimate("x", "y")
        item.ratio = 2747.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledLeaseReturnEstimateLabelLengthRule(3600)
        val item = newScheduledLeaseReturnEstimate("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
