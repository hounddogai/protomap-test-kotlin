package com.acme.transport.safety.nearmiss.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissLedgerStore()
        val item = newNearMissLedger("code-318", "name-997")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
