package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterLedgerStore()
        val item = newRosterLedger("code-235", "label-159")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
