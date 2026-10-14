package com.acme.transport.network.cutoff.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictCutoffSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictCutoffSnapshotStore()
        val item = newStrictCutoffSnapshot("code-837", "name-575")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
