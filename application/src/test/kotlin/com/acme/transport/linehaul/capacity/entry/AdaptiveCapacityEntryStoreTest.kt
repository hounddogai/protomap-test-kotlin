package com.acme.transport.linehaul.capacity.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCapacityEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCapacityEntryStore()
        val item = newAdaptiveCapacityEntry("code-331", "title-727")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
