package com.acme.transport.freightpay.freightinvoice.invoicerollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFreightInvoiceRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newGlobalFreightInvoiceRollup("code-168", "name-892")
        assertEquals(GlobalFreightInvoiceRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newGlobalFreightInvoiceRollup("code-168", "name-892")
        try {
            item.moveTo(GlobalFreightInvoiceRollupStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFreightInvoiceRollupStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newGlobalFreightInvoiceRollup("code-168", "name-892")
        item.status = GlobalFreightInvoiceRollupStatus.STOPPED
        assertFails("move") { item.moveTo(GlobalFreightInvoiceRollupStatus.IDLE) }
    }
}
