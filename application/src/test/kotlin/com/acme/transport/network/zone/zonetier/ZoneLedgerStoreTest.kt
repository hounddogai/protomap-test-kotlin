package com.acme.transport.network.zone.zonetier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneLedgerStore()
        val item = newZoneLedger("code-221", "title-113")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
