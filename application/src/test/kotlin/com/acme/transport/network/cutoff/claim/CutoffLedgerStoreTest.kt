package com.acme.transport.network.cutoff.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffLedgerStore()
        val item = newCutoffLedger("code-158", "name-697")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
