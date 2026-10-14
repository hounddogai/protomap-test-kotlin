package com.acme.transport.crewing.roster.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRosterRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRosterRecordStore()
        val item = newTieredRosterRecord("code-587", "name-746")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
