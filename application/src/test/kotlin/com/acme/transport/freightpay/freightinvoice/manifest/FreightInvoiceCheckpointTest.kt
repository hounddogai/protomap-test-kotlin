package com.acme.transport.freightpay.freightinvoice.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceCheckpointTest {
    @Test
    fun `starts in planned`() {
        val item = newFreightInvoiceCheckpoint("code-892", "name-888")
        assertEquals(FreightInvoiceCheckpointStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newFreightInvoiceCheckpoint("code-892", "name-888")
        try {
            item.moveTo(FreightInvoiceCheckpointStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceCheckpointStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFreightInvoiceCheckpoint("code-892", "name-888")
        item.status = FreightInvoiceCheckpointStatus.CANCELLED
        assertFails("move") { item.moveTo(FreightInvoiceCheckpointStatus.PLANNED) }
    }
}
