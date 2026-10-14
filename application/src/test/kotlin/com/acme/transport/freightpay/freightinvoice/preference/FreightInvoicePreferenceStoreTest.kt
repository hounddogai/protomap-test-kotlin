package com.acme.transport.freightpay.freightinvoice.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoicePreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoicePreferenceStore()
        val item = newFreightInvoicePreference("code-923", "label-115")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
