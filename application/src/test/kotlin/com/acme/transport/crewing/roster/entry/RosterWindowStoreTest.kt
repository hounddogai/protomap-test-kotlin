package com.acme.transport.crewing.roster.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterWindowStore()
        val item = newRosterWindow("code-524", "name-45")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
