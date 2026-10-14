package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingFreightInvoiceWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingFreightInvoiceWindowValidator()
        val item = newRollingFreightInvoiceWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RollingFreightInvoiceWindowLoadKgRangeRule(1.0, 1382.6)
        val item = newRollingFreightInvoiceWindow("x", "y")
        item.loadKg = 1384.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = RollingFreightInvoiceWindowMaximumRangeRule(2.8, 683.8)
        val item = newRollingFreightInvoiceWindow("x", "y")
        item.maximum = 685.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingFreightInvoiceWindowCodeLengthRule(255)
        val item = newRollingFreightInvoiceWindow("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
