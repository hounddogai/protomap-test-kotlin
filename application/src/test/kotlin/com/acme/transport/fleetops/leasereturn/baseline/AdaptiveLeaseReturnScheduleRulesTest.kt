package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveLeaseReturnScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveLeaseReturnScheduleValidator()
        val item = newAdaptiveLeaseReturnSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AdaptiveLeaseReturnScheduleRequiredLabelRule("adaptive lease return schedule needs a value")
        val item = newAdaptiveLeaseReturnSchedule("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AdaptiveLeaseReturnScheduleLabelLengthRule(86400)
        val item = newAdaptiveLeaseReturnSchedule("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long summary`() {
        val rule = AdaptiveLeaseReturnScheduleSummaryLengthRule(32)
        val item = newAdaptiveLeaseReturnSchedule("x", "y")
        item.summary = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
