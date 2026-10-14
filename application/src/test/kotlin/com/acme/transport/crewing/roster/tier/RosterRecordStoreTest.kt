package com.acme.transport.crewing.roster.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterRecordStore()
        val item = newRosterRecord("code-948", "title-842")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
