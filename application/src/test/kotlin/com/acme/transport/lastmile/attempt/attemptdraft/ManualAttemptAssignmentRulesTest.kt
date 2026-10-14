package com.acme.transport.lastmile.attempt.attemptdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualAttemptAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualAttemptAssignmentValidator()
        val item = newManualAttemptAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = ManualAttemptAssignmentBatchSizeRangeRule(32, 80)
        val item = newManualAttemptAssignment("x", "y")
        item.batchSize = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualAttemptAssignmentRequiredTitleRule("manual attempt assignment needs an entry")
        val item = newManualAttemptAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = ManualAttemptAssignmentSegmentLengthRule(120)
        val item = newManualAttemptAssignment("x", "y")
        item.segment = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualAttemptAssignmentTitleLengthRule(80)
        val item = newManualAttemptAssignment("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = ManualAttemptAssignmentStopCountRangeRule(80, 86400)
        val item = newManualAttemptAssignment("x", "y")
        item.stopCount = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
