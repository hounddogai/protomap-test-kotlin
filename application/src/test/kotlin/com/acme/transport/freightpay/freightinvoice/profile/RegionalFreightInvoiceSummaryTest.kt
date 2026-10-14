package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalFreightInvoiceSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalFreightInvoiceSummary("code-977", "title-971")
        assertEquals(RegionalFreightInvoiceSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRegionalFreightInvoiceSummary("code-977", "title-971")
        try {
            item.moveTo(RegionalFreightInvoiceSummaryStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalFreightInvoiceSummaryStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalFreightInvoiceSummary("code-977", "title-971")
        item.status = RegionalFreightInvoiceSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalFreightInvoiceSummaryStatus.DRAFT) }
    }
}
