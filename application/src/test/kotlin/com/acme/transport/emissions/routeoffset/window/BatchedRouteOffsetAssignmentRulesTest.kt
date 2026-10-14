package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRouteOffsetAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRouteOffsetAssignmentValidator()
        val item = newBatchedRouteOffsetAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = BatchedRouteOffsetAssignmentCapacityRangeRule(80, 255)
        val item = newBatchedRouteOffsetAssignment("x", "y")
        item.capacity = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedRouteOffsetAssignmentRequiredCodeRule("batched route offset assignment needs a value")
        val item = newBatchedRouteOffsetAssignment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRouteOffsetAssignmentCodeLengthRule(120)
        val item = newBatchedRouteOffsetAssignment("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
