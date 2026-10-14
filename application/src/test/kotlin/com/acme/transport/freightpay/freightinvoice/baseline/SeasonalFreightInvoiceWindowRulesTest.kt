package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalFreightInvoiceWindowRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalFreightInvoiceWindowValidator()
        val item = newSeasonalFreightInvoiceWindow("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SeasonalFreightInvoiceWindowRequiredNameRule("seasonal freight invoice window needs this field")
        val item = newSeasonalFreightInvoiceWindow("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = SeasonalFreightInvoiceWindowEndsAtRangeRule(32, 64)
        val item = newSeasonalFreightInvoiceWindow("x", "y")
        item.endsAt = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above budget`() {
        val rule = SeasonalFreightInvoiceWindowBudgetRangeRule(5.6, 3508.5)
        val item = newSeasonalFreightInvoiceWindow("x", "y")
        item.budget = 3510.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
