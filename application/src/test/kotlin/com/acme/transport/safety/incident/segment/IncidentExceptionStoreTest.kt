package com.acme.transport.safety.incident.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentExceptionStore()
        val item = newIncidentException("code-986", "label-16")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
