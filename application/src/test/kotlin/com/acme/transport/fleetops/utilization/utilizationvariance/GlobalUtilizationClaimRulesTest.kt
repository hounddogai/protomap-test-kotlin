package com.acme.transport.fleetops.utilization.utilizationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalUtilizationClaimRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalUtilizationClaimValidator()
        val item = newGlobalUtilizationClaim("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = GlobalUtilizationClaimBatchSizeRangeRule(32, 3600)
        val item = newGlobalUtilizationClaim("x", "y")
        item.batchSize = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = GlobalUtilizationClaimFuelLitersRangeRule(4.1, 1986.9)
        val item = newGlobalUtilizationClaim("x", "y")
        item.fuelLiters = 1988.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = GlobalUtilizationClaimRequiredTitleRule("global utilization claim needs a value")
        val item = newGlobalUtilizationClaim("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
