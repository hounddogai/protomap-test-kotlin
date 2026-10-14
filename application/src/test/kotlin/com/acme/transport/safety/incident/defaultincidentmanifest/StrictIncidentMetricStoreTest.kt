package com.acme.transport.safety.incident.defaultincidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictIncidentMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictIncidentMetricStore()
        val item = newStrictIncidentMetric("code-776", "label-102")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
