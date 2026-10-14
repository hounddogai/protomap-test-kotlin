package com.acme.transport.freightpay.freightinvoice.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FreightInvoiceManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFreightInvoiceManifestStore()
        val item = newFreightInvoiceManifest("code-378", "title-763")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
