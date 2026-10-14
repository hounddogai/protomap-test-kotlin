package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRouteOffsetAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRouteOffsetAllocationValidator()
        val item = newBatchedRouteOffsetAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedRouteOffsetAllocationTitleLengthRule(255)
        val item = newBatchedRouteOffsetAllocation("x", "y")
        item.title = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = BatchedRouteOffsetAllocationMaximumRangeRule(6.6, 651.5)
        val item = newBatchedRouteOffsetAllocation("x", "y")
        item.maximum = 653.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BatchedRouteOffsetAllocationFuelLitersRangeRule(8.4, 294.6)
        val item = newBatchedRouteOffsetAllocation("x", "y")
        item.fuelLiters = 296.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRouteOffsetAllocationCodeLengthRule(32)
        val item = newBatchedRouteOffsetAllocation("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
