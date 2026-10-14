package com.acme.transport.fleetops.defect.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDefectManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDefectManifestStore()
        val item = newTieredDefectManifest("code-287", "title-298")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
