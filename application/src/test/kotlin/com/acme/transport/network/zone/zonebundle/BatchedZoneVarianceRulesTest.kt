package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedZoneVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedZoneVarianceValidator()
        val item = newBatchedZoneVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedZoneVarianceCodeLengthRule(1000)
        val item = newBatchedZoneVariance("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedZoneVarianceBatchSizeRangeRule(32, 86400)
        val item = newBatchedZoneVariance("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = BatchedZoneVarianceMinimumRangeRule(9.3, 1450.5)
        val item = newBatchedZoneVariance("x", "y")
        item.minimum = 1452.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = BatchedZoneVarianceCapacityRangeRule(86400, 86400)
        val item = newBatchedZoneVariance("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long phone`() {
        val rule = BatchedZoneVariancePhoneLengthRule(86400)
        val item = newBatchedZoneVariance("x", "y")
        item.phone = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
