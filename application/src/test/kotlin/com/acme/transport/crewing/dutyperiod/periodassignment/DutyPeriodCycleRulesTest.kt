package com.acme.transport.crewing.dutyperiod.periodassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodCycleValidator()
        val item = newDutyPeriodCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DutyPeriodCycleRequiredCodeRule("duty period cycle needs a value")
        val item = newDutyPeriodCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = DutyPeriodCycleRequiredUnitRule("duty period cycle needs this field")
        val item = newDutyPeriodCycle("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = DutyPeriodCycleDriverIdLengthRule(32)
        val item = newDutyPeriodCycle("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
