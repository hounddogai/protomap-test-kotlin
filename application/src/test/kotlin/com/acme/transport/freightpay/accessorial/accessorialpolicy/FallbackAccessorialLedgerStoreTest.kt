package com.acme.transport.freightpay.accessorial.accessorialpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAccessorialLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAccessorialLedgerStore()
        val item = newFallbackAccessorialLedger("code-229", "label-374")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
