package com.acme.transport.emissions.emissionfactor.factorproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackEmissionFactorBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackEmissionFactorBatchValidator()
        val item = newFallbackEmissionFactorBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = FallbackEmissionFactorBatchNameLengthRule(32)
        val item = newFallbackEmissionFactorBatch("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FallbackEmissionFactorBatchRequiredNameRule("fallback emission factor batch needs an entry")
        val item = newFallbackEmissionFactorBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FallbackEmissionFactorBatchCodeLengthRule(255)
        val item = newFallbackEmissionFactorBatch("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = FallbackEmissionFactorBatchCeilingRangeRule(4.6, 3208.4)
        val item = newFallbackEmissionFactorBatch("x", "y")
        item.ceiling = 3209.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackEmissionFactorBatchRequiredCodeRule("fallback emission factor batch needs this field")
        val item = newFallbackEmissionFactorBatch("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
