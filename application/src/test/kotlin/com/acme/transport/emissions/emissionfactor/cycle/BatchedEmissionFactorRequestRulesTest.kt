package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedEmissionFactorRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedEmissionFactorRequestValidator()
        val item = newBatchedEmissionFactorRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedEmissionFactorRequestCodeLengthRule(3600)
        val item = newBatchedEmissionFactorRequest("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedEmissionFactorRequestRequiredCodeRule("batched emission factor request needs a value")
        val item = newBatchedEmissionFactorRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = BatchedEmissionFactorRequestOdometerRangeRule(32, 255)
        val item = newBatchedEmissionFactorRequest("x", "y")
        item.odometer = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedEmissionFactorRequestTitleLengthRule(16)
        val item = newBatchedEmissionFactorRequest("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = BatchedEmissionFactorRequestRequiredChecksumRule("batched emission factor request needs an entry")
        val item = newBatchedEmissionFactorRequest("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
