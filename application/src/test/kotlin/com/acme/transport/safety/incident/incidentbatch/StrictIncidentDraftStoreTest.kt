package com.acme.transport.safety.incident.incidentbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictIncidentDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictIncidentDraftStore()
        val item = newStrictIncidentDraft("code-437", "name-980")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
