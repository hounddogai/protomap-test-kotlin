package com.acme.transport.linehaul.capacity.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityEntryStore()
        val item = newCapacityEntry("code-531", "title-115")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
