package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceThresholdTest {
    @Test
    fun `starts in idle`() {
        val item = newFreightInvoiceThreshold("code-298", "label-544")
        assertEquals(FreightInvoiceThresholdStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFreightInvoiceThreshold("code-298", "label-544")
        try {
            item.moveTo(FreightInvoiceThresholdStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceThresholdStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFreightInvoiceThreshold("code-298", "label-544")
        item.status = FreightInvoiceThresholdStatus.STOPPED
        assertFails("move") { item.moveTo(FreightInvoiceThresholdStatus.IDLE) }
    }
}
