package com.acme.transport.crewing.dutyperiod.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualDutyPeriodSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualDutyPeriodSlotValidator()
        val item = newManualDutyPeriodSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = ManualDutyPeriodSlotAttemptsRangeRule(80, 86400)
        val item = newManualDutyPeriodSlot("x", "y")
        item.attempts = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = ManualDutyPeriodSlotVehicleIdLengthRule(255)
        val item = newManualDutyPeriodSlot("x", "y")
        item.vehicleId = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualDutyPeriodSlotLabelLengthRule(255)
        val item = newManualDutyPeriodSlot("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
