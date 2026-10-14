package com.acme.transport.safety.incident.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentManifestStore()
        val item = newIncidentManifest("code-167", "title-177")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
