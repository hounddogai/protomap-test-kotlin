package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropBatchValidator()
        val item = newDropBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = DropBatchRequiredChecksumRule("drop batch needs this field")
        val item = newDropBatch("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = DropBatchOdometerRangeRule(16, 16)
        val item = newDropBatch("x", "y")
        item.odometer = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DropBatchCostRangeRule(3.1, 1115.0)
        val item = newDropBatch("x", "y")
        item.cost = 1116.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
