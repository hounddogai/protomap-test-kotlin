package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubSnapshotStore()
        val item = newHubSnapshot("code-322", "title-176")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
