package com.acme.transport.freightpay.freightinvoice.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FreightInvoiceAllocationTest {
    @Test
    fun `starts in idle`() {
        val item = newFreightInvoiceAllocation("code-273", "name-480")
        assertEquals(FreightInvoiceAllocationStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newFreightInvoiceAllocation("code-273", "name-480")
        try {
            item.moveTo(FreightInvoiceAllocationStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FreightInvoiceAllocationStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newFreightInvoiceAllocation("code-273", "name-480")
        item.status = FreightInvoiceAllocationStatus.STOPPED
        assertFails("move") { item.moveTo(FreightInvoiceAllocationStatus.IDLE) }
    }
}
