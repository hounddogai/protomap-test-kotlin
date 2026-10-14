package com.acme.transport.freightpay.freightinvoice.strictinvoicemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingFreightInvoiceRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingFreightInvoiceRuleStore()
        val item = newPendingFreightInvoiceRule("code-687", "title-834")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
