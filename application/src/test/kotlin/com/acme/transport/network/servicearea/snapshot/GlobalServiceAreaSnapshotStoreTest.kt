package com.acme.transport.network.servicearea.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalServiceAreaSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalServiceAreaSnapshotStore()
        val item = newGlobalServiceAreaSnapshot("code-662", "name-852")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
