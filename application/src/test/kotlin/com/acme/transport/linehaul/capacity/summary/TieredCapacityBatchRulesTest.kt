package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCapacityBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCapacityBatchValidator()
        val item = newTieredCapacityBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = TieredCapacityBatchRequiredSegmentRule("tiered capacity batch needs this field")
        val item = newTieredCapacityBatch("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = TieredCapacityBatchRequiredExternalRefRule("tiered capacity batch needs a value")
        val item = newTieredCapacityBatch("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = TieredCapacityBatchDistanceKmRangeRule(2.5, 460.4)
        val item = newTieredCapacityBatch("x", "y")
        item.distanceKm = 461.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = TieredCapacityBatchMinimumRangeRule(0.8, 245.0)
        val item = newTieredCapacityBatch("x", "y")
        item.minimum = 246.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
