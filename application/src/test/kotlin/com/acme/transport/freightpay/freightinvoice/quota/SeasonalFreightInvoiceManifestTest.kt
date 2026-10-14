package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalFreightInvoiceManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalFreightInvoiceManifest("code-516", "label-989")
        assertEquals(SeasonalFreightInvoiceManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newSeasonalFreightInvoiceManifest("code-516", "label-989")
        try {
            item.moveTo(SeasonalFreightInvoiceManifestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalFreightInvoiceManifestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalFreightInvoiceManifest("code-516", "label-989")
        item.status = SeasonalFreightInvoiceManifestStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalFreightInvoiceManifestStatus.DRAFT) }
    }
}
