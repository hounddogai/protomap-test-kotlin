package com.acme.transport.linehaul.capacity.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCapacityEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCapacityEntryStore()
        val item = newPendingCapacityEntry("code-998", "label-57")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
