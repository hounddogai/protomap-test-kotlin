package com.acme.transport.freightpay.freightinvoice.invoicereview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientFreightInvoiceProfileTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientFreightInvoiceProfile("code-355", "name-380")
        assertEquals(LenientFreightInvoiceProfileStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLenientFreightInvoiceProfile("code-355", "name-380")
        try {
            item.moveTo(LenientFreightInvoiceProfileStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientFreightInvoiceProfileStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientFreightInvoiceProfile("code-355", "name-380")
        item.status = LenientFreightInvoiceProfileStatus.STOPPED
        assertFails("move") { item.moveTo(LenientFreightInvoiceProfileStatus.IDLE) }
    }
}
