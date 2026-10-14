package com.acme.transport.lastmile.drop.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropLedgerStore()
        val item = newDropLedger("code-30", "name-982")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
