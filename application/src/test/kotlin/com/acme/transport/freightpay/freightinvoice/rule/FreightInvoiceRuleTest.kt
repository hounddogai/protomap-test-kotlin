package com.acme.transport.freightpay.freightinvoice.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newFreightInvoiceRule("code-888", "name-174")
        assertEquals(FreightInvoiceRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newFreightInvoiceRule("code-888", "name-174")
        try {
            item.moveTo(FreightInvoiceRuleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceRuleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFreightInvoiceRule("code-888", "name-174")
        item.status = FreightInvoiceRuleStatus.CLOSED
        assertFails("move") { item.moveTo(FreightInvoiceRuleStatus.DRAFT) }
    }
}
