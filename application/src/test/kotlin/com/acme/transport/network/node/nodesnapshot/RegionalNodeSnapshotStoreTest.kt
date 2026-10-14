package com.acme.transport.network.node.nodesnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalNodeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalNodeSnapshotStore()
        val item = newRegionalNodeSnapshot("code-900", "name-302")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
