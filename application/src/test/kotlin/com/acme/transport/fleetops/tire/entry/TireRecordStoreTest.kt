package com.acme.transport.fleetops.tire.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireRecordStore()
        val item = newTireRecord("code-679", "label-534")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
