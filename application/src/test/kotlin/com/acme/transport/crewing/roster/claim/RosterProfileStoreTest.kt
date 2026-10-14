package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterProfileStore()
        val item = newRosterProfile("code-400", "label-49")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
