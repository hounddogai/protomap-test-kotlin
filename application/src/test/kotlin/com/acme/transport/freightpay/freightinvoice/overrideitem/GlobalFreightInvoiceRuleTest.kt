package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFreightInvoiceRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalFreightInvoiceRule("code-101", "name-631")
        assertEquals(GlobalFreightInvoiceRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newGlobalFreightInvoiceRule("code-101", "name-631")
        try {
            item.moveTo(GlobalFreightInvoiceRuleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFreightInvoiceRuleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalFreightInvoiceRule("code-101", "name-631")
        item.status = GlobalFreightInvoiceRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalFreightInvoiceRuleStatus.REQUESTED) }
    }
}
