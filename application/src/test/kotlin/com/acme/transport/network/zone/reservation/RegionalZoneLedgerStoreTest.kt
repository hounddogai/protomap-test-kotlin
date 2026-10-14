package com.acme.transport.network.zone.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalZoneLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalZoneLedgerStore()
        val item = newRegionalZoneLedger("code-717", "title-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
