package com.acme.transport.freightpay.freightinvoice.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledFreightInvoiceSnapshotTest {
    @Test
    fun `starts in draft`() {
        val item = newScheduledFreightInvoiceSnapshot("code-982", "title-664")
        assertEquals(ScheduledFreightInvoiceSnapshotStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newScheduledFreightInvoiceSnapshot("code-982", "title-664")
        try {
            item.moveTo(ScheduledFreightInvoiceSnapshotStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledFreightInvoiceSnapshotStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newScheduledFreightInvoiceSnapshot("code-982", "title-664")
        item.status = ScheduledFreightInvoiceSnapshotStatus.CLOSED
        assertFails("move") { item.moveTo(ScheduledFreightInvoiceSnapshotStatus.DRAFT) }
    }
}
