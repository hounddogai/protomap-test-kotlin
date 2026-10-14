package com.acme.transport.safety.incident.defaultincidentmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyIncidentManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyIncidentManifestStore()
        val item = newLegacyIncidentManifest("code-980", "title-202")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
