package com.acme.transport.fleetops.tire.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireManifestStore()
        val item = newTireManifest("code-58", "name-355")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
