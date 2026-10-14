package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRosterEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRosterEntryStore()
        val item = newGlobalRosterEntry("code-248", "name-497")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
