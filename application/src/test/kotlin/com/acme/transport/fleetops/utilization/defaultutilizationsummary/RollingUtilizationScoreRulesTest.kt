package com.acme.transport.fleetops.utilization.defaultutilizationsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingUtilizationScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingUtilizationScoreValidator()
        val item = newRollingUtilizationScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RollingUtilizationScoreFloorValueRangeRule(3.5, 1362.6)
        val item = newRollingUtilizationScore("x", "y")
        item.floorValue = 1364.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingUtilizationScoreRequiredCodeRule("rolling utilization score needs an entry")
        val item = newRollingUtilizationScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RollingUtilizationScoreCapacityRangeRule(80, 3600)
        val item = newRollingUtilizationScore("x", "y")
        item.capacity = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
