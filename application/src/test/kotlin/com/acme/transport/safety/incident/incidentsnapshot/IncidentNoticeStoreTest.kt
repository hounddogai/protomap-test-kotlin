package com.acme.transport.safety.incident.incidentsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentNoticeStore()
        val item = newIncidentNotice("code-921", "name-366")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
