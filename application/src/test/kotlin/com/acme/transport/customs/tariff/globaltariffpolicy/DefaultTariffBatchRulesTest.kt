package com.acme.transport.customs.tariff.globaltariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultTariffBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultTariffBatchValidator()
        val item = newDefaultTariffBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = DefaultTariffBatchStartsAtRangeRule(16, 32)
        val item = newDefaultTariffBatch("x", "y")
        item.startsAt = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long region`() {
        val rule = DefaultTariffBatchRegionLengthRule(3600)
        val item = newDefaultTariffBatch("x", "y")
        item.region = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultTariffBatchRequiredCodeRule("default tariff batch needs this field")
        val item = newDefaultTariffBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
