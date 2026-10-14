package com.acme.transport.freightpay.freightinvoice.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredFreightInvoiceQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredFreightInvoiceQuota("code-359", "name-990")
        assertEquals(TieredFreightInvoiceQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredFreightInvoiceQuota("code-359", "name-990")
        try {
            item.moveTo(TieredFreightInvoiceQuotaStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredFreightInvoiceQuotaStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredFreightInvoiceQuota("code-359", "name-990")
        item.status = TieredFreightInvoiceQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredFreightInvoiceQuotaStatus.REQUESTED) }
    }
}
