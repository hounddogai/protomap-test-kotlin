package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilitySnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilitySnapshotStore()
        val item = newAvailabilitySnapshot("code-515", "name-820")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
