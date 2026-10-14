package com.acme.transport.freightpay.freightinvoice.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackFreightInvoiceCheckpointTest {
    @Test
    fun `starts in new`() {
        val item = newFallbackFreightInvoiceCheckpoint("code-325", "name-731")
        assertEquals(FallbackFreightInvoiceCheckpointStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newFallbackFreightInvoiceCheckpoint("code-325", "name-731")
        try {
            item.moveTo(FallbackFreightInvoiceCheckpointStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackFreightInvoiceCheckpointStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newFallbackFreightInvoiceCheckpoint("code-325", "name-731")
        item.status = FallbackFreightInvoiceCheckpointStatus.FAILED
        assertFails("move") { item.moveTo(FallbackFreightInvoiceCheckpointStatus.NEW) }
    }
}
