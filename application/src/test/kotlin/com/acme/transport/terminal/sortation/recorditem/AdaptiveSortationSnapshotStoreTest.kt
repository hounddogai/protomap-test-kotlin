package com.acme.transport.terminal.sortation.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveSortationSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveSortationSnapshotStore()
        val item = newAdaptiveSortationSnapshot("code-674", "name-663")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
