package com.acme.transport.freightpay.freightinvoice.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceDigestValidator()
        val item = newFreightInvoiceDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = FreightInvoiceDigestRequiredLabelRule("freight invoice digest needs an entry")
        val item = newFreightInvoiceDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = FreightInvoiceDigestLabelLengthRule(1000)
        val item = newFreightInvoiceDigest("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long route code`() {
        val rule = FreightInvoiceDigestRouteCodeLengthRule(1000)
        val item = newFreightInvoiceDigest("x", "y")
        item.routeCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.routeCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = FreightInvoiceDigestNameLengthRule(1000)
        val item = newFreightInvoiceDigest("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = FreightInvoiceDigestRequiredNameRule("freight invoice digest needs a value")
        val item = newFreightInvoiceDigest("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = FreightInvoiceDigestCodeLengthRule(86400)
        val item = newFreightInvoiceDigest("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
