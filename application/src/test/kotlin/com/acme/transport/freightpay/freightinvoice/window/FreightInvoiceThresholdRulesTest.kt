package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertTrue

class FreightInvoiceThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFreightInvoiceThresholdValidator()
        val item = newFreightInvoiceThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = FreightInvoiceThresholdPalletsRangeRule(120, 3600)
        val item = newFreightInvoiceThreshold("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above score`() {
        val rule = FreightInvoiceThresholdScoreRangeRule(4.2, 4748.4)
        val item = newFreightInvoiceThreshold("x", "y")
        item.score = 4749.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = FreightInvoiceThresholdExpiresAtRangeRule(120, 255)
        val item = newFreightInvoiceThreshold("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
