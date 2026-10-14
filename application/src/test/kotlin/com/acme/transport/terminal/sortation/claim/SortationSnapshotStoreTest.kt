package com.acme.transport.terminal.sortation.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationSnapshotStore()
        val item = newSortationSnapshot("code-806", "name-829")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
