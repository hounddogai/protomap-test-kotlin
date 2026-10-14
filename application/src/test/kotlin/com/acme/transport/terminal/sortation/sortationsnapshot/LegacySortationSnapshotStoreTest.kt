package com.acme.transport.terminal.sortation.sortationsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacySortationSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacySortationSnapshotStore()
        val item = newLegacySortationSnapshot("code-972", "label-703")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
