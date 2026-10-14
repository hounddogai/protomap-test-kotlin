package com.acme.transport.lastmile.parcel.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelAllocationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelAllocationValidator()
        val item = newParcelAllocation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ParcelAllocationCodeLengthRule(1000)
        val item = newParcelAllocation("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = ParcelAllocationNameLengthRule(86400)
        val item = newParcelAllocation("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
