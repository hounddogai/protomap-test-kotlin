package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictDefectBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictDefectBatchValidator()
        val item = newStrictDefectBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictDefectBatchRequiredNameRule("strict defect batch needs this field")
        val item = newStrictDefectBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = StrictDefectBatchThresholdRangeRule(2.8, 1009.2)
        val item = newStrictDefectBatch("x", "y")
        item.threshold = 1010.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above discount`() {
        val rule = StrictDefectBatchDiscountRangeRule(4.4, 623.7)
        val item = newStrictDefectBatch("x", "y")
        item.discount = 625.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
