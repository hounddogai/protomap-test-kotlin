package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentPlanStore()
        val item = newIncidentPlan("code-909", "label-408")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
