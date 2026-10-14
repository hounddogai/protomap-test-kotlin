package com.acme.transport.linehaul.capacity.capacityallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyCapacitySnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyCapacitySnapshotStore()
        val item = newLegacyCapacitySnapshot("code-893", "name-36")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
