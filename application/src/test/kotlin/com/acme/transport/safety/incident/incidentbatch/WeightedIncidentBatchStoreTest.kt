package com.acme.transport.safety.incident.incidentbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedIncidentBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedIncidentBatchStore()
        val item = newWeightedIncidentBatch("code-451", "label-681")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
