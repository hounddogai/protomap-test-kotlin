package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredZoneManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredZoneManifestStore()
        val item = newTieredZoneManifest("code-931", "label-109")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
