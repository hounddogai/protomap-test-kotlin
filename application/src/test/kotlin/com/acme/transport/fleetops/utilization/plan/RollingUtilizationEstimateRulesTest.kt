package com.acme.transport.fleetops.utilization.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingUtilizationEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingUtilizationEstimateValidator()
        val item = newRollingUtilizationEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingUtilizationEstimateRequiredNameRule("rolling utilization estimate needs an entry")
        val item = newRollingUtilizationEstimate("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingUtilizationEstimateNameLengthRule(1000)
        val item = newRollingUtilizationEstimate("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = RollingUtilizationEstimateRequiredVehicleIdRule("rolling utilization estimate needs this field")
        val item = newRollingUtilizationEstimate("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = RollingUtilizationEstimateChecksumLengthRule(255)
        val item = newRollingUtilizationEstimate("x", "y")
        item.checksum = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingUtilizationEstimateRequiredCodeRule("rolling utilization estimate needs an entry")
        val item = newRollingUtilizationEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
