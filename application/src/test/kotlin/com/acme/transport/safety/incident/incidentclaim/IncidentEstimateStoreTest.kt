package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentEstimateStore()
        val item = newIncidentEstimate("code-700", "title-111")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
