package com.acme.transport.terminal.crossdock.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockLedgerStore()
        val item = newCrossDockLedger("code-203", "title-201")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
