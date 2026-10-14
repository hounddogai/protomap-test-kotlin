package com.acme.transport.linehaul.capacity.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityLedgerStore()
        val item = newCapacityLedger("code-118", "label-435")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
