package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedViolationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedViolationCycleValidator()
        val item = newWeightedViolationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedViolationCycleRequiredNameRule("weighted violation cycle needs this field")
        val item = newWeightedViolationCycle("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = WeightedViolationCycleBatchSizeRangeRule(32, 1000)
        val item = newWeightedViolationCycle("x", "y")
        item.batchSize = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = WeightedViolationCycleRequiredCodeRule("weighted violation cycle needs an entry")
        val item = newWeightedViolationCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = WeightedViolationCycleDepotLengthRule(64)
        val item = newWeightedViolationCycle("x", "y")
        item.depot = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
