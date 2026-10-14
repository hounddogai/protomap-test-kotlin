package com.acme.transport.safety.incident.incidenttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingIncidentBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingIncidentBaselineStore()
        val item = newRollingIncidentBaseline("code-137", "title-554")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
