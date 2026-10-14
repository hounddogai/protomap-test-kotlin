package com.acme.transport.freightpay.freightinvoice.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultFreightInvoiceBatchTest {
    @Test
    fun `starts in created`() {
        val item = newDefaultFreightInvoiceBatch("code-499", "name-775")
        assertEquals(DefaultFreightInvoiceBatchStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDefaultFreightInvoiceBatch("code-499", "name-775")
        try {
            item.moveTo(DefaultFreightInvoiceBatchStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultFreightInvoiceBatchStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDefaultFreightInvoiceBatch("code-499", "name-775")
        item.status = DefaultFreightInvoiceBatchStatus.RETIRED
        assertFails("move") { item.moveTo(DefaultFreightInvoiceBatchStatus.CREATED) }
    }
}
