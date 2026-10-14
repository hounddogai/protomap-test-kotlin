package com.acme.transport.freightpay.freightinvoice.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingFreightInvoiceWindowTest {
    @Test
    fun `starts in new`() {
        val item = newRollingFreightInvoiceWindow("code-889", "label-867")
        assertEquals(RollingFreightInvoiceWindowStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRollingFreightInvoiceWindow("code-889", "label-867")
        try {
            item.moveTo(RollingFreightInvoiceWindowStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingFreightInvoiceWindowStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingFreightInvoiceWindow("code-889", "label-867")
        item.status = RollingFreightInvoiceWindowStatus.FAILED
        assertFails("move") { item.moveTo(RollingFreightInvoiceWindowStatus.NEW) }
    }
}
