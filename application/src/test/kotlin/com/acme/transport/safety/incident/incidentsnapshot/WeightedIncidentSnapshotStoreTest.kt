package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedIncidentSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedIncidentSnapshotStore()
        val item = newWeightedIncidentSnapshot("code-505", "label-75")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
