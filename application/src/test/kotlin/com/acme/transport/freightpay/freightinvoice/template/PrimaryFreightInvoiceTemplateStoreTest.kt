package com.acme.transport.freightpay.freightinvoice.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryFreightInvoiceTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryFreightInvoiceTemplateStore()
        val item = newPrimaryFreightInvoiceTemplate("code-429", "name-515")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
