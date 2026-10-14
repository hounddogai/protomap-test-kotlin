package com.acme.transport.safety.nearmiss.missclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultNearMissLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultNearMissLedgerStore()
        val item = newDefaultNearMissLedger("code-504", "title-554")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
