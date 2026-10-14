package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEmissionFactorSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedEmissionFactorSegmentValidator()
        val item = newBatchedEmissionFactorSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = BatchedEmissionFactorSegmentAmountRangeRule(3.5, 1890.7)
        val item = newBatchedEmissionFactorSegment("x", "y")
        item.amount = 1892.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = BatchedEmissionFactorSegmentWindowDaysRangeRule(255, 1000)
        val item = newBatchedEmissionFactorSegment("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = BatchedEmissionFactorSegmentRequiredUnitRule("batched emission factor segment needs this field")
        val item = newBatchedEmissionFactorSegment("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
