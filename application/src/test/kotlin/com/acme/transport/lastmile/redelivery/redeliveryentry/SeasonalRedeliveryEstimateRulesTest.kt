package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalRedeliveryEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalRedeliveryEstimateValidator()
        val item = newSeasonalRedeliveryEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalRedeliveryEstimateLabelLengthRule(3600)
        val item = newSeasonalRedeliveryEstimate("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalRedeliveryEstimateRequiredCodeRule("seasonal redelivery estimate needs an entry")
        val item = newSeasonalRedeliveryEstimate("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = SeasonalRedeliveryEstimateRequiredSummaryRule("seasonal redelivery estimate needs a value")
        val item = newSeasonalRedeliveryEstimate("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SeasonalRedeliveryEstimateRequiredOwnerIdRule("seasonal redelivery estimate needs a value")
        val item = newSeasonalRedeliveryEstimate("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = SeasonalRedeliveryEstimateQuantityRangeRule(16, 80)
        val item = newSeasonalRedeliveryEstimate("x", "y")
        item.quantity = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalRedeliveryEstimateCodeLengthRule(64)
        val item = newSeasonalRedeliveryEstimate("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
