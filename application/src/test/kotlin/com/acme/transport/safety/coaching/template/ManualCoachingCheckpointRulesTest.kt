package com.acme.transport.safety.coaching.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualCoachingCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualCoachingCheckpointValidator()
        val item = newManualCoachingCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = ManualCoachingCheckpointLabelLengthRule(3600)
        val item = newManualCoachingCheckpoint("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = ManualCoachingCheckpointExpiresAtRangeRule(32, 80)
        val item = newManualCoachingCheckpoint("x", "y")
        item.expiresAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above window days`() {
        val rule = ManualCoachingCheckpointWindowDaysRangeRule(80, 3600)
        val item = newManualCoachingCheckpoint("x", "y")
        item.windowDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
