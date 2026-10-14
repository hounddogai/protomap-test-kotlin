package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedNearMissLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedNearMissLedgerStore()
        val item = newCappedNearMissLedger("code-668", "title-380")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
