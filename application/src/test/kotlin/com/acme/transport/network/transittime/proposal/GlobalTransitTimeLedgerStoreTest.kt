package com.acme.transport.network.transittime.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTransitTimeLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTransitTimeLedgerStore()
        val item = newGlobalTransitTimeLedger("code-27", "name-787")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
