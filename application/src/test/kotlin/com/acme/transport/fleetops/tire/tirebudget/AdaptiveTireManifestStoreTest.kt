package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTireManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTireManifestStore()
        val item = newAdaptiveTireManifest("code-654", "title-867")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
