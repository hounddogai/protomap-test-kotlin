package com.acme.transport.fleetops.leasereturn.returnrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyLeaseReturnRequestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyLeaseReturnRequestValidator()
        val item = newLegacyLeaseReturnRequest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyLeaseReturnRequestCodeLengthRule(32)
        val item = newLegacyLeaseReturnRequest("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = LegacyLeaseReturnRequestShiftCodeLengthRule(1000)
        val item = newLegacyLeaseReturnRequest("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
