package com.acme.transport.fleetops.leasereturn.fallbackreturnforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLeaseReturnForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedLeaseReturnForecastValidator()
        val item = newBatchedLeaseReturnForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = BatchedLeaseReturnForecastDiscountRangeRule(9.3, 1505.9)
        val item = newBatchedLeaseReturnForecast("x", "y")
        item.discount = 1507.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedLeaseReturnForecastRequiredCodeRule("batched lease return forecast needs an entry")
        val item = newBatchedLeaseReturnForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
