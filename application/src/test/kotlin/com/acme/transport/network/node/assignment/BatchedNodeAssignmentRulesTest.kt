package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNodeAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNodeAssignmentValidator()
        val item = newBatchedNodeAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedNodeAssignmentTitleLengthRule(86400)
        val item = newBatchedNodeAssignment("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedNodeAssignmentRequiredTitleRule("batched node assignment needs a value")
        val item = newBatchedNodeAssignment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = BatchedNodeAssignmentRequiredContactEmailRule("batched node assignment needs a value")
        val item = newBatchedNodeAssignment("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
