package com.acme.transport.freightpay.freightinvoice.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFreightInvoiceTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredFreightInvoiceTarget("code-560", "name-331")
        assertEquals(TieredFreightInvoiceTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredFreightInvoiceTarget("code-560", "name-331")
        try {
            item.moveTo(TieredFreightInvoiceTargetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFreightInvoiceTargetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredFreightInvoiceTarget("code-560", "name-331")
        item.status = TieredFreightInvoiceTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredFreightInvoiceTargetStatus.REQUESTED) }
    }
}
