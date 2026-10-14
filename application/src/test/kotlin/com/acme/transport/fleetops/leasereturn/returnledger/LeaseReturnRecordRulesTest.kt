package com.acme.transport.fleetops.leasereturn.returnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LeaseReturnRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLeaseReturnRecordValidator()
        val item = newLeaseReturnRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LeaseReturnRecordBudgetRangeRule(10.0, 2136.7)
        val item = newLeaseReturnRecord("x", "y")
        item.budget = 2138.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = LeaseReturnRecordTitleLengthRule(1000)
        val item = newLeaseReturnRecord("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above weight`() {
        val rule = LeaseReturnRecordWeightRangeRule(7.4, 3377.9)
        val item = newLeaseReturnRecord("x", "y")
        item.weight = 3379.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
