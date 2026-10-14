package com.acme.transport.safety.incident.incidentproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientIncidentAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientIncidentAdjustmentStore()
        val item = newLenientIncidentAdjustment("code-413", "name-775")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
