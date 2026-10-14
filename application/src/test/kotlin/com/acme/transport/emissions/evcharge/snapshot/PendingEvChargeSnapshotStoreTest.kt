package com.acme.transport.emissions.evcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingEvChargeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingEvChargeSnapshotStore()
        val item = newPendingEvChargeSnapshot("code-495", "label-694")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
