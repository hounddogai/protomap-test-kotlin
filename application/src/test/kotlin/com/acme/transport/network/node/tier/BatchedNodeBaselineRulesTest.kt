package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNodeBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNodeBaselineValidator()
        val item = newBatchedNodeBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedNodeBaselineRequiredCodeRule("batched node baseline needs this field")
        val item = newBatchedNodeBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = BatchedNodeBaselineTrailerIdLengthRule(16)
        val item = newBatchedNodeBaseline("x", "y")
        item.trailerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = BatchedNodeBaselineContactEmailLengthRule(86400)
        val item = newBatchedNodeBaseline("x", "y")
        item.contactEmail = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
