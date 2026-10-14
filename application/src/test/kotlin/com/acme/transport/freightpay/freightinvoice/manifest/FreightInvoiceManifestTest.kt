package com.acme.transport.freightpay.freightinvoice.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newFreightInvoiceManifest("code-378", "title-763")
        assertEquals(FreightInvoiceManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newFreightInvoiceManifest("code-378", "title-763")
        try {
            item.moveTo(FreightInvoiceManifestStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceManifestStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFreightInvoiceManifest("code-378", "title-763")
        item.status = FreightInvoiceManifestStatus.CLOSED
        assertFails("move") { item.moveTo(FreightInvoiceManifestStatus.DRAFT) }
    }
}
