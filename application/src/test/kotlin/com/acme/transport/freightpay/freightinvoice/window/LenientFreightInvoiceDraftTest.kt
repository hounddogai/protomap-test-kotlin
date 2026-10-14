package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFreightInvoiceDraftTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientFreightInvoiceDraft("code-2", "label-118")
        assertEquals(LenientFreightInvoiceDraftStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientFreightInvoiceDraft("code-2", "label-118")
        try {
            item.moveTo(LenientFreightInvoiceDraftStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFreightInvoiceDraftStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientFreightInvoiceDraft("code-2", "label-118")
        item.status = LenientFreightInvoiceDraftStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientFreightInvoiceDraftStatus.PROPOSED) }
    }
}
