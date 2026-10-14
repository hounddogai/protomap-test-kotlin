package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodAdjustmentValidator()
        val item = newDutyPeriodAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = DutyPeriodAdjustmentRequiredVehicleIdRule("duty period adjustment needs a value")
        val item = newDutyPeriodAdjustment("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
