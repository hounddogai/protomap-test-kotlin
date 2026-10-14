package com.acme.transport.safety.incident.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentBatchStore()
        val item = newIncidentBatch("code-358", "label-894")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
