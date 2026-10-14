package com.acme.transport.safety.incident.incidentclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentAdjustmentStore()
        val item = newIncidentAdjustment("code-887", "label-17")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
