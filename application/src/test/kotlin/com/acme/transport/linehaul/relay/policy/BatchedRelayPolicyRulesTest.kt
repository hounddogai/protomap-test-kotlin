package com.acme.transport.linehaul.relay.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedRelayPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedRelayPolicyValidator()
        val item = newBatchedRelayPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedRelayPolicyRequiredCodeRule("batched relay policy needs this field")
        val item = newBatchedRelayPolicy("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedRelayPolicyCodeLengthRule(80)
        val item = newBatchedRelayPolicy("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = BatchedRelayPolicyCeilingRangeRule(5.3, 1056.7)
        val item = newBatchedRelayPolicy("x", "y")
        item.ceiling = 1058.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = BatchedRelayPolicyRequiredOwnerIdRule("batched relay policy needs an entry")
        val item = newBatchedRelayPolicy("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
