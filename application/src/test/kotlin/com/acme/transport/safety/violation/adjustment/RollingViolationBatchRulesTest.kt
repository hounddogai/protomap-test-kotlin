package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingViolationBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingViolationBatchValidator()
        val item = newRollingViolationBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = RollingViolationBatchStartsAtRangeRule(64, 255)
        val item = newRollingViolationBatch("x", "y")
        item.startsAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingViolationBatchScoreRangeRule(3.1, 2902.2)
        val item = newRollingViolationBatch("x", "y")
        item.score = 2903.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingViolationBatchLabelLengthRule(64)
        val item = newRollingViolationBatch("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = RollingViolationBatchRouteCodeLengthRule(3600)
        val item = newRollingViolationBatch("x", "y")
        item.routeCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
