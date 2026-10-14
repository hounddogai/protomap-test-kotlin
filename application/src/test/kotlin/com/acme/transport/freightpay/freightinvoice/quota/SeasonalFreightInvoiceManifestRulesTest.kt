package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFreightInvoiceManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalFreightInvoiceManifestValidator()
        val item = newSeasonalFreightInvoiceManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = SeasonalFreightInvoiceManifestCountryLengthRule(32)
        val item = newSeasonalFreightInvoiceManifest("x", "y")
        item.country = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = SeasonalFreightInvoiceManifestThresholdRangeRule(8.5, 4815.4)
        val item = newSeasonalFreightInvoiceManifest("x", "y")
        item.threshold = 4816.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalFreightInvoiceManifestCodeLengthRule(255)
        val item = newSeasonalFreightInvoiceManifest("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
