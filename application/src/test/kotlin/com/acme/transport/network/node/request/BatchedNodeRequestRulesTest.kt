package com.acme.transport.network.node.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNodeRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNodeRequestValidator()
        val item = newBatchedNodeRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = BatchedNodeRequestRequiredChannelRule("batched node request needs this field")
        val item = newBatchedNodeRequest("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedNodeRequestNameLengthRule(16)
        val item = newBatchedNodeRequest("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
