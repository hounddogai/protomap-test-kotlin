package com.acme.transport.freightpay.freightinvoice.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveFreightInvoiceManifestTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveFreightInvoiceManifest("code-591", "name-202")
        assertEquals(AdaptiveFreightInvoiceManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAdaptiveFreightInvoiceManifest("code-591", "name-202")
        try {
            item.moveTo(AdaptiveFreightInvoiceManifestStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveFreightInvoiceManifestStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveFreightInvoiceManifest("code-591", "name-202")
        item.status = AdaptiveFreightInvoiceManifestStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveFreightInvoiceManifestStatus.NEW) }
    }
}
