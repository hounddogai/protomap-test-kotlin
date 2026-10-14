package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireLedgerStore()
        val item = newTireLedger("code-979", "name-920")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
