package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalSortationLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalSortationLedgerStore()
        val item = newRegionalSortationLedger("code-128", "name-919")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
