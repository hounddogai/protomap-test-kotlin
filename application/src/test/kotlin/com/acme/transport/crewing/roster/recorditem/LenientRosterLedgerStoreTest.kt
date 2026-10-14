package com.acme.transport.crewing.roster.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRosterLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRosterLedgerStore()
        val item = newLenientRosterLedger("code-448", "name-991")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
