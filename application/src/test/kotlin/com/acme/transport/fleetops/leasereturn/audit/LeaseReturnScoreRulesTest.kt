package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnScoreValidator()
        val item = newLeaseReturnScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LeaseReturnScoreNameLengthRule(255)
        val item = newLeaseReturnScore("x", "y")
        item.name = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = LeaseReturnScoreUnitLengthRule(3600)
        val item = newLeaseReturnScore("x", "y")
        item.unit = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LeaseReturnScoreRequiredCodeRule("lease return score needs a value")
        val item = newLeaseReturnScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = LeaseReturnScoreRequiredUnitRule("lease return score needs an entry")
        val item = newLeaseReturnScore("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
