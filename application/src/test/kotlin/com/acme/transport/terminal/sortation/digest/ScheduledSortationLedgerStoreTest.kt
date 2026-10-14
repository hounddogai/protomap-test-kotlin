package com.acme.transport.terminal.sortation.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledSortationLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledSortationLedgerStore()
        val item = newScheduledSortationLedger("code-478", "label-421")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
