package com.acme.transport.freightpay.freightinvoice.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newFreightInvoiceSnapshot("code-181", "name-582")
        assertEquals(FreightInvoiceSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFreightInvoiceSnapshot("code-181", "name-582")
        try {
            item.moveTo(FreightInvoiceSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFreightInvoiceSnapshot("code-181", "name-582")
        item.status = FreightInvoiceSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FreightInvoiceSnapshotStatus.PROPOSED) }
    }
}
