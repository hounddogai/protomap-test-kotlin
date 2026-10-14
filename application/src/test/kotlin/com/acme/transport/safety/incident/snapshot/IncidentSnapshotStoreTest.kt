package com.acme.transport.safety.incident.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentSnapshotStore()
        val item = newIncidentSnapshot("code-314", "title-361")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
