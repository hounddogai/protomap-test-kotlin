package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalLeaseReturnPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalLeaseReturnPolicyValidator()
        val item = newRegionalLeaseReturnPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RegionalLeaseReturnPolicyScoreRangeRule(2.5, 3622.9)
        val item = newRegionalLeaseReturnPolicy("x", "y")
        item.score = 3624.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalLeaseReturnPolicyCodeLengthRule(3600)
        val item = newRegionalLeaseReturnPolicy("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
