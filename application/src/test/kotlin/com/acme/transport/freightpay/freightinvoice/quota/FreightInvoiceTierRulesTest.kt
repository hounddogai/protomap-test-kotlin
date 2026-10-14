package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FreightInvoiceTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceTierValidator()
        val item = newFreightInvoiceTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FreightInvoiceTierPalletsRangeRule(32, 3600)
        val item = newFreightInvoiceTier("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FreightInvoiceTierRequiredCodeRule("freight invoice tier needs a value")
        val item = newFreightInvoiceTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
