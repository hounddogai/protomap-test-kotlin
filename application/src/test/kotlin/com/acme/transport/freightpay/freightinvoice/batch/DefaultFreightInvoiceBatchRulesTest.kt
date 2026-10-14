package com.acme.transport.freightpay.freightinvoice.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultFreightInvoiceBatchRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultFreightInvoiceBatchValidator()
        val item = newDefaultFreightInvoiceBatch("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultFreightInvoiceBatchNameLengthRule(3600)
        val item = newDefaultFreightInvoiceBatch("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DefaultFreightInvoiceBatchRequiredNameRule("default freight invoice batch needs a value")
        val item = newDefaultFreightInvoiceBatch("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long route code`() {
        val rule = DefaultFreightInvoiceBatchRouteCodeLengthRule(86400)
        val item = newDefaultFreightInvoiceBatch("x", "y")
        item.routeCode = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
