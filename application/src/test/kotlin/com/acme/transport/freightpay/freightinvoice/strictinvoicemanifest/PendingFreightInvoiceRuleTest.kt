package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFreightInvoiceRuleTest {
    @Test
    fun `starts in created`() {
        val item = newPendingFreightInvoiceRule("code-687", "title-834")
        assertEquals(PendingFreightInvoiceRuleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newPendingFreightInvoiceRule("code-687", "title-834")
        try {
            item.moveTo(PendingFreightInvoiceRuleStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFreightInvoiceRuleStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingFreightInvoiceRule("code-687", "title-834")
        item.status = PendingFreightInvoiceRuleStatus.RETIRED
        assertFails("move") { item.moveTo(PendingFreightInvoiceRuleStatus.CREATED) }
    }
}
