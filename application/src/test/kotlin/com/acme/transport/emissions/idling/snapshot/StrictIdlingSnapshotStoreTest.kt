package com.acme.transport.emissions.idling.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictIdlingSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictIdlingSnapshotStore()
        val item = newStrictIdlingSnapshot("code-125", "label-112")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
