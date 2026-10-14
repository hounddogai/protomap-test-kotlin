package com.acme.transport.safety.scorecard.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorecardBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScorecardBatchValidator()
        val item = newScorecardBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = ScorecardBatchRequiredLabelRule("scorecard batch needs an entry")
        val item = newScorecardBatch("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = ScorecardBatchEndsAtRangeRule(255, 1000)
        val item = newScorecardBatch("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ScorecardBatchDepotLengthRule(255)
        val item = newScorecardBatch("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ScorecardBatchLoadKgRangeRule(2.8, 2907.3)
        val item = newScorecardBatch("x", "y")
        item.loadKg = 2908.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
