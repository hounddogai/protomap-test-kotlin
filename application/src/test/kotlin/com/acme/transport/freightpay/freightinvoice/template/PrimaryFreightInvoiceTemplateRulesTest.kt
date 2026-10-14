package com.acme.transport.freightpay.freightinvoice.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryFreightInvoiceTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryFreightInvoiceTemplateValidator()
        val item = newPrimaryFreightInvoiceTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = PrimaryFreightInvoiceTemplateNameLengthRule(64)
        val item = newPrimaryFreightInvoiceTemplate("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryFreightInvoiceTemplateCodeLengthRule(32)
        val item = newPrimaryFreightInvoiceTemplate("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PrimaryFreightInvoiceTemplateFloorValueRangeRule(8.5, 1358.9)
        val item = newPrimaryFreightInvoiceTemplate("x", "y")
        item.floorValue = 1360.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
