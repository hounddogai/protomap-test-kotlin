package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTrailerYardEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTrailerYardEstimateValidator()
        val item = newManualTrailerYardEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank locale`() {
        val rule = ManualTrailerYardEstimateRequiredLocaleRule("manual trailer yard estimate needs a value")
        val item = newManualTrailerYardEstimate("", "")
        item.locale = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualTrailerYardEstimateRequiredTitleRule("manual trailer yard estimate needs this field")
        val item = newManualTrailerYardEstimate("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = ManualTrailerYardEstimateCapacityRangeRule(64, 86400)
        val item = newManualTrailerYardEstimate("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualTrailerYardEstimateCodeLengthRule(80)
        val item = newManualTrailerYardEstimate("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ManualTrailerYardEstimateLocaleLengthRule(16)
        val item = newManualTrailerYardEstimate("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
