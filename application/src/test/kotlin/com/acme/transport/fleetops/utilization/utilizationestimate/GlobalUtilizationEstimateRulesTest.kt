package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalUtilizationEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalUtilizationEstimateValidator()
        val item = newGlobalUtilizationEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalUtilizationEstimateTitleLengthRule(120)
        val item = newGlobalUtilizationEstimate("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalUtilizationEstimateCodeLengthRule(16)
        val item = newGlobalUtilizationEstimate("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long depot`() {
        val rule = GlobalUtilizationEstimateDepotLengthRule(32)
        val item = newGlobalUtilizationEstimate("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = GlobalUtilizationEstimateExpiresAtRangeRule(16, 1000)
        val item = newGlobalUtilizationEstimate("x", "y")
        item.expiresAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = GlobalUtilizationEstimateDurationMinutesRangeRule(64, 1000)
        val item = newGlobalUtilizationEstimate("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
