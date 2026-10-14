package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryIncidentSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryIncidentSegmentStore()
        val item = newPrimaryIncidentSegment("code-824", "title-316")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
