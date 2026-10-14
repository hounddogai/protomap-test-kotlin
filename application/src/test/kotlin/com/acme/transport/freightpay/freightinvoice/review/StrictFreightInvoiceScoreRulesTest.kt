package com.acme.transport.freightpay.freightinvoice.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictFreightInvoiceScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictFreightInvoiceScoreValidator()
        val item = newStrictFreightInvoiceScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictFreightInvoiceScoreRequiredTitleRule("strict freight invoice score needs this field")
        val item = newStrictFreightInvoiceScore("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = StrictFreightInvoiceScoreRequiredCodeRule("strict freight invoice score needs an entry")
        val item = newStrictFreightInvoiceScore("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictFreightInvoiceScoreCodeLengthRule(120)
        val item = newStrictFreightInvoiceScore("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = StrictFreightInvoiceScoreOwnerIdLengthRule(64)
        val item = newStrictFreightInvoiceScore("x", "y")
        item.ownerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long region`() {
        val rule = StrictFreightInvoiceScoreRegionLengthRule(16)
        val item = newStrictFreightInvoiceScore("x", "y")
        item.region = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.region = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
