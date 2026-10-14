package com.acme.transport.freightpay.freightinvoice.invoicequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFreightInvoiceQuotaTest {
    @Test
    fun `starts in idle`() {
        val item = newRollingFreightInvoiceQuota("code-774", "name-850")
        assertEquals(RollingFreightInvoiceQuotaStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRollingFreightInvoiceQuota("code-774", "name-850")
        try {
            item.moveTo(RollingFreightInvoiceQuotaStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFreightInvoiceQuotaStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRollingFreightInvoiceQuota("code-774", "name-850")
        item.status = RollingFreightInvoiceQuotaStatus.STOPPED
        assertFails("move") { item.moveTo(RollingFreightInvoiceQuotaStatus.IDLE) }
    }
}
