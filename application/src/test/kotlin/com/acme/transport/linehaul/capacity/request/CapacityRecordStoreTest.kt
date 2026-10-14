package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityRecordStore()
        val item = newCapacityRecord("code-900", "name-683")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
