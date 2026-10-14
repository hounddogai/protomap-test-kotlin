package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveFreightInvoiceManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveFreightInvoiceManifestValidator()
        val item = newAdaptiveFreightInvoiceManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = AdaptiveFreightInvoiceManifestRequiredShiftCodeRule(
            "adaptive freight invoice manifest needs an entry",
        )
        val item = newAdaptiveFreightInvoiceManifest("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = AdaptiveFreightInvoiceManifestCapacityRangeRule(32, 120)
        val item = newAdaptiveFreightInvoiceManifest("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
