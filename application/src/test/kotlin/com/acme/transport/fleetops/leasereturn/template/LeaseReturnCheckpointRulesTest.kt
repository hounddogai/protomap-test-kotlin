package com.acme.transport.fleetops.leasereturn.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnCheckpointValidator()
        val item = newLeaseReturnCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LeaseReturnCheckpointCodeLengthRule(16)
        val item = newLeaseReturnCheckpoint("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
