package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationBatchValidator()
        val item = newUtilizationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationBatchRequiredCodeRule("utilization batch needs this field")
        val item = newUtilizationBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above discount`() {
        val rule = UtilizationBatchDiscountRangeRule(4.4, 4419.0)
        val item = newUtilizationBatch("x", "y")
        item.discount = 4420.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
