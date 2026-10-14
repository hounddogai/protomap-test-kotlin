package com.acme.transport.freightpay.freightinvoice.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingFreightInvoiceBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingFreightInvoiceBaselineStore()
        val item = newRollingFreightInvoiceBaseline("code-836", "title-987")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
