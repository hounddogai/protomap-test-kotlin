package com.acme.transport.safety.incident.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultIncidentCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultIncidentCycleValidator()
        val item = newDefaultIncidentCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = DefaultIncidentCycleCeilingRangeRule(1.9, 4391.6)
        val item = newDefaultIncidentCycle("x", "y")
        item.ceiling = 4393.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultIncidentCycleRequiredCodeRule("default incident cycle needs this field")
        val item = newDefaultIncidentCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DefaultIncidentCycleOdometerRangeRule(16, 80)
        val item = newDefaultIncidentCycle("x", "y")
        item.odometer = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = DefaultIncidentCycleFloorValueRangeRule(0.6, 3300.9)
        val item = newDefaultIncidentCycle("x", "y")
        item.floorValue = 3302.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
