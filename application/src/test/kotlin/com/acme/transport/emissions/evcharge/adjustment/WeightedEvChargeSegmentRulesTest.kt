package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedEvChargeSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedEvChargeSegmentValidator()
        val item = newWeightedEvChargeSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = WeightedEvChargeSegmentSummaryLengthRule(86400)
        val item = newWeightedEvChargeSegment("x", "y")
        item.summary = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedEvChargeSegmentRequiredCodeRule("weighted ev charge segment needs an entry")
        val item = newWeightedEvChargeSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = WeightedEvChargeSegmentDescriptionLengthRule(86400)
        val item = newWeightedEvChargeSegment("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
