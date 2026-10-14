package com.acme.transport.network.zone.pendingzonemapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictZoneRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictZoneRecordStore()
        val item = newStrictZoneRecord("code-389", "title-353")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
