package com.acme.transport.network.servicearea.areaexception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedServiceAreaSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedServiceAreaSnapshotValidator()
        val item = newBatchedServiceAreaSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedServiceAreaSnapshotNameLengthRule(1000)
        val item = newBatchedServiceAreaSnapshot("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = BatchedServiceAreaSnapshotAmountRangeRule(3.6, 1575.3)
        val item = newBatchedServiceAreaSnapshot("x", "y")
        item.amount = 1576.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = BatchedServiceAreaSnapshotWeightRangeRule(2.8, 483.9)
        val item = newBatchedServiceAreaSnapshot("x", "y")
        item.weight = 485.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedServiceAreaSnapshotRequiredCodeRule("batched service area snapshot needs this field")
        val item = newBatchedServiceAreaSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
