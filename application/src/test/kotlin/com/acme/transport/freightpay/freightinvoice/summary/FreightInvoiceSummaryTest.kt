package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newFreightInvoiceSummary("code-602", "title-344")
        assertEquals(FreightInvoiceSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newFreightInvoiceSummary("code-602", "title-344")
        try {
            item.moveTo(FreightInvoiceSummaryStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceSummaryStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newFreightInvoiceSummary("code-602", "title-344")
        item.status = FreightInvoiceSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(FreightInvoiceSummaryStatus.CREATED) }
    }
}
