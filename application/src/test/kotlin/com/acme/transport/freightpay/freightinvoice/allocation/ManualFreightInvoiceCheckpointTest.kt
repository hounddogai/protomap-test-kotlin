package com.acme.transport.freightpay.freightinvoice.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualFreightInvoiceCheckpointTest {
    @Test
    fun `starts in idle`() {
        val item = newManualFreightInvoiceCheckpoint("code-194", "name-689")
        assertEquals(ManualFreightInvoiceCheckpointStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newManualFreightInvoiceCheckpoint("code-194", "name-689")
        try {
            item.moveTo(ManualFreightInvoiceCheckpointStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualFreightInvoiceCheckpointStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newManualFreightInvoiceCheckpoint("code-194", "name-689")
        item.status = ManualFreightInvoiceCheckpointStatus.STOPPED
        assertFails("move") { item.moveTo(ManualFreightInvoiceCheckpointStatus.IDLE) }
    }
}
