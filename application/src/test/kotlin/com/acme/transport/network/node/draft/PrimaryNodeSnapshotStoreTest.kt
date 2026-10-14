package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryNodeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryNodeSnapshotStore()
        val item = newPrimaryNodeSnapshot("code-847", "title-608")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
