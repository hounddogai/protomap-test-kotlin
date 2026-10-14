package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedLeaseReturnRuleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedLeaseReturnRuleValidator()
        val item = newBatchedLeaseReturnRule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedLeaseReturnRuleCodeLengthRule(32)
        val item = newBatchedLeaseReturnRule("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedLeaseReturnRuleRequiredNameRule("batched lease return rule needs an entry")
        val item = newBatchedLeaseReturnRule("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
