package com.acme.transport.crewing.roster.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterDigestStore()
        val item = newRosterDigest("code-333", "name-649")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
