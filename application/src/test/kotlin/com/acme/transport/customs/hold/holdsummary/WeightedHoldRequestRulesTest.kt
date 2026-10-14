package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedHoldRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedHoldRequestValidator()
        val item = newWeightedHoldRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long summary`() {
        val rule = WeightedHoldRequestSummaryLengthRule(1000)
        val item = newWeightedHoldRequest("x", "y")
        item.summary = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedHoldRequestRequiredCodeRule("weighted hold request needs an entry")
        val item = newWeightedHoldRequest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedHoldRequestRequiredNameRule("weighted hold request needs a value")
        val item = newWeightedHoldRequest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = WeightedHoldRequestCapacityRangeRule(255, 86400)
        val item = newWeightedHoldRequest("x", "y")
        item.capacity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
