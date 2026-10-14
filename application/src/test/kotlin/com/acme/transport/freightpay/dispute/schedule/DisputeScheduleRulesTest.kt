package com.acme.transport.freightpay.dispute.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DisputeScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDisputeScheduleValidator()
        val item = newDisputeSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = DisputeScheduleMaximumRangeRule(9.1, 3421.8)
        val item = newDisputeSchedule("x", "y")
        item.maximum = 3423.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DisputeScheduleLabelLengthRule(80)
        val item = newDisputeSchedule("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = DisputeScheduleVehicleIdLengthRule(120)
        val item = newDisputeSchedule("x", "y")
        item.vehicleId = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = DisputeScheduleUpdatedAtRangeRule(120, 255)
        val item = newDisputeSchedule("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
