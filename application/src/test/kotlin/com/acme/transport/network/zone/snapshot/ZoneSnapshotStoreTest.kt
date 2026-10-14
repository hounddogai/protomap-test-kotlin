package com.acme.transport.network.zone.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneSnapshotStore()
        val item = newZoneSnapshot("code-52", "title-268")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
