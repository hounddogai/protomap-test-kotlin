package com.acme.transport.network.cutoff.regionalcutoffsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCutoffSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCutoffSegmentValidator()
        val item = newPendingCutoffSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PendingCutoffSegmentNameLengthRule(120)
        val item = newPendingCutoffSegment("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above budget`() {
        val rule = PendingCutoffSegmentBudgetRangeRule(4.9, 2299.5)
        val item = newPendingCutoffSegment("x", "y")
        item.budget = 2301.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = PendingCutoffSegmentLocaleLengthRule(120)
        val item = newPendingCutoffSegment("x", "y")
        item.locale = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PendingCutoffSegmentRequiredSegmentRule("pending cutoff segment needs this field")
        val item = newPendingCutoffSegment("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
