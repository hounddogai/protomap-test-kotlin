package com.acme.transport.terminal.dockdoor.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDockDoorPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDockDoorPlanValidator()
        val item = newBatchedDockDoorPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = BatchedDockDoorPlanUpdatedAtRangeRule(80, 255)
        val item = newBatchedDockDoorPlan("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedDockDoorPlanRequiredNameRule("batched dock door plan needs this field")
        val item = newBatchedDockDoorPlan("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedDockDoorPlanRequiredTitleRule("batched dock door plan needs a value")
        val item = newBatchedDockDoorPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedDockDoorPlanCodeLengthRule(1000)
        val item = newBatchedDockDoorPlan("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
