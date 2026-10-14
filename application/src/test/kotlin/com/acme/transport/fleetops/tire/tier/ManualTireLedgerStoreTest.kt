package com.acme.transport.fleetops.tire.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTireLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTireLedgerStore()
        val item = newManualTireLedger("code-409", "label-96")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
