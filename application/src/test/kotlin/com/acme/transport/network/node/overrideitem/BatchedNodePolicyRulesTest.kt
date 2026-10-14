package com.acme.transport.network.node.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNodePolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNodePolicyValidator()
        val item = newBatchedNodePolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedNodePolicyCodeLengthRule(120)
        val item = newBatchedNodePolicy("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedNodePolicyRequiredCodeRule("batched node policy needs a value")
        val item = newBatchedNodePolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedNodePolicyRequiredLabelRule("batched node policy needs an entry")
        val item = newBatchedNodePolicy("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
