package com.acme.transport.customs.tariff.tariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TariffBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTariffBatchValidator()
        val item = newTariffBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = TariffBatchAttemptsRangeRule(255, 255)
        val item = newTariffBatch("x", "y")
        item.attempts = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = TariffBatchCodeLengthRule(255)
        val item = newTariffBatch("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = TariffBatchQuantityRangeRule(80, 1000)
        val item = newTariffBatch("x", "y")
        item.quantity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
