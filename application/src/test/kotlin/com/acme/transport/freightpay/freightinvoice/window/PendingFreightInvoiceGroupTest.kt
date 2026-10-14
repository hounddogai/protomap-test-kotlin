package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingFreightInvoiceGroupTest {
    @Test
    fun `starts in created`() {
        val item = newPendingFreightInvoiceGroup("code-905", "title-469")
        assertEquals(PendingFreightInvoiceGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPendingFreightInvoiceGroup("code-905", "title-469")
        try {
            item.moveTo(PendingFreightInvoiceGroupStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingFreightInvoiceGroupStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPendingFreightInvoiceGroup("code-905", "title-469")
        item.status = PendingFreightInvoiceGroupStatus.RETIRED
        assertFails("move") { item.moveTo(PendingFreightInvoiceGroupStatus.CREATED) }
    }
}
