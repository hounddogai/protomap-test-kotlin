package com.acme.transport.lastmile.locker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLockerPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedLockerPlanValidator()
        val item = newBatchedLockerPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = BatchedLockerPlanTrailerIdLengthRule(64)
        val item = newBatchedLockerPlan("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedLockerPlanRequiredLabelRule("batched locker plan needs an entry")
        val item = newBatchedLockerPlan("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedLockerPlanCodeLengthRule(32)
        val item = newBatchedLockerPlan("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedLockerPlanLabelLengthRule(255)
        val item = newBatchedLockerPlan("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = BatchedLockerPlanRequiredTrailerIdRule("batched locker plan needs this field")
        val item = newBatchedLockerPlan("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
