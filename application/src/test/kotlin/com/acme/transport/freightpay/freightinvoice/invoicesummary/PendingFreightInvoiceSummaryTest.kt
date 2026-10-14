package com.acme.transport.freightpay.freightinvoice.invoicesummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFreightInvoiceSummaryTest {
    @Test
    fun `starts in idle`() {
        val item = newPendingFreightInvoiceSummary("code-655", "label-301")
        assertEquals(PendingFreightInvoiceSummaryStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newPendingFreightInvoiceSummary("code-655", "label-301")
        try {
            item.moveTo(PendingFreightInvoiceSummaryStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFreightInvoiceSummaryStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPendingFreightInvoiceSummary("code-655", "label-301")
        item.status = PendingFreightInvoiceSummaryStatus.STOPPED
        assertFails("move") { item.moveTo(PendingFreightInvoiceSummaryStatus.IDLE) }
    }
}
