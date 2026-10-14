package com.acme.transport.safety.incident.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyIncidentVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyIncidentVarianceStore()
        val item = newLegacyIncidentVariance("code-164", "label-645")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
