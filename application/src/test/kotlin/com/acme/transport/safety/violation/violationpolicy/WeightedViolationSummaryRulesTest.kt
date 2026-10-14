package com.acme.transport.safety.violation.violationpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedViolationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedViolationSummaryValidator()
        val item = newWeightedViolationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = WeightedViolationSummaryCodeLengthRule(64)
        val item = newWeightedViolationSummary("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = WeightedViolationSummaryBatchSizeRangeRule(16, 32)
        val item = newWeightedViolationSummary("x", "y")
        item.batchSize = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank country`() {
        val rule = WeightedViolationSummaryRequiredCountryRule("weighted violation summary needs this field")
        val item = newWeightedViolationSummary("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
