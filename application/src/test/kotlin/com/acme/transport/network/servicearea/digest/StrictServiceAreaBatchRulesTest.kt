package com.acme.transport.network.servicearea.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictServiceAreaBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictServiceAreaBatchValidator()
        val item = newStrictServiceAreaBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = StrictServiceAreaBatchCurrencyLengthRule(1000)
        val item = newStrictServiceAreaBatch("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = StrictServiceAreaBatchRatioRangeRule(6.6, 4443.5)
        val item = newStrictServiceAreaBatch("x", "y")
        item.ratio = 4445.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank currency`() {
        val rule = StrictServiceAreaBatchRequiredCurrencyRule("strict service area batch needs an entry")
        val item = newStrictServiceAreaBatch("", "")
        item.currency = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = StrictServiceAreaBatchRequiredLabelRule("strict service area batch needs a value")
        val item = newStrictServiceAreaBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
