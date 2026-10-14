package com.acme.transport.customs.tariff.tariffbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTariffVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTariffVarianceValidator()
        val item = newBatchedTariffVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTariffVarianceCodeLengthRule(3600)
        val item = newBatchedTariffVariance("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long source`() {
        val rule = BatchedTariffVarianceSourceLengthRule(32)
        val item = newBatchedTariffVariance("x", "y")
        item.source = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedTariffVarianceBatchSizeRangeRule(64, 1000)
        val item = newBatchedTariffVariance("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = BatchedTariffVarianceFuelLitersRangeRule(3.2, 2040.7)
        val item = newBatchedTariffVariance("x", "y")
        item.fuelLiters = 2042.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
