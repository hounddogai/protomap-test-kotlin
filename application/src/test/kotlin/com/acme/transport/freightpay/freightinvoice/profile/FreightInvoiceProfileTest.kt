package com.acme.transport.freightpay.freightinvoice.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceProfileTest {
    @Test
    fun `starts in planned`() {
        val item = newFreightInvoiceProfile("code-14", "title-497")
        assertEquals(FreightInvoiceProfileStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newFreightInvoiceProfile("code-14", "title-497")
        try {
            item.moveTo(FreightInvoiceProfileStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceProfileStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFreightInvoiceProfile("code-14", "title-497")
        item.status = FreightInvoiceProfileStatus.CANCELLED
        assertFails("move") { item.moveTo(FreightInvoiceProfileStatus.PLANNED) }
    }
}
