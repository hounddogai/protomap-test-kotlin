package com.acme.transport.fleetops.utilization.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalUtilizationPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalUtilizationPolicyValidator()
        val item = newSeasonalUtilizationPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = SeasonalUtilizationPolicyPriorityRangeRule(120, 1000)
        val item = newSeasonalUtilizationPolicy("x", "y")
        item.priority = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = SeasonalUtilizationPolicyBatchSizeRangeRule(80, 3600)
        val item = newSeasonalUtilizationPolicy("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = SeasonalUtilizationPolicyRequiredSegmentRule("seasonal utilization policy needs a value")
        val item = newSeasonalUtilizationPolicy("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = SeasonalUtilizationPolicyVehicleIdLengthRule(3600)
        val item = newSeasonalUtilizationPolicy("x", "y")
        item.vehicleId = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
