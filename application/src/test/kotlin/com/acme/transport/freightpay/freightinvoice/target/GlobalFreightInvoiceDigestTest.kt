package com.acme.transport.freightpay.freightinvoice.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalFreightInvoiceDigestTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalFreightInvoiceDigest("code-722", "name-226")
        assertEquals(GlobalFreightInvoiceDigestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newGlobalFreightInvoiceDigest("code-722", "name-226")
        try {
            item.moveTo(GlobalFreightInvoiceDigestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalFreightInvoiceDigestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalFreightInvoiceDigest("code-722", "name-226")
        item.status = GlobalFreightInvoiceDigestStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalFreightInvoiceDigestStatus.REQUESTED) }
    }
}
