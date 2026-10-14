package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTireSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTireSnapshotStore()
        val item = newCappedTireSnapshot("code-720", "name-812")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
