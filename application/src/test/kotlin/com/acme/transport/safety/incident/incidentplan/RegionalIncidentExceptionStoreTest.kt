package com.acme.transport.safety.incident.incidentplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalIncidentExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalIncidentExceptionStore()
        val item = newRegionalIncidentException("code-369", "name-529")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
