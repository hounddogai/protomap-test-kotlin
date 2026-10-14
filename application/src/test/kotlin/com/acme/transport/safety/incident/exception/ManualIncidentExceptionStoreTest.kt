package com.acme.transport.safety.incident.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualIncidentExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualIncidentExceptionStore()
        val item = newManualIncidentException("code-892", "title-993")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
