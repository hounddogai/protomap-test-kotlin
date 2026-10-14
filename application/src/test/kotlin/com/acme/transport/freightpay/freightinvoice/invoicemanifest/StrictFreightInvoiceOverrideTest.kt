package com.acme.transport.freightpay.freightinvoice.invoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictFreightInvoiceOverrideTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictFreightInvoiceOverride("code-954", "name-873")
        assertEquals(StrictFreightInvoiceOverrideStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newStrictFreightInvoiceOverride("code-954", "name-873")
        try {
            item.moveTo(StrictFreightInvoiceOverrideStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictFreightInvoiceOverrideStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictFreightInvoiceOverride("code-954", "name-873")
        item.status = StrictFreightInvoiceOverrideStatus.STOPPED
        assertFails("move") { item.moveTo(StrictFreightInvoiceOverrideStatus.IDLE) }
    }
}
