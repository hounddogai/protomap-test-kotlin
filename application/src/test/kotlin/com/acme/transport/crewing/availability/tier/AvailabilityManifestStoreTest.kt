package com.acme.transport.crewing.availability.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityManifestStore()
        val item = newAvailabilityManifest("code-904", "name-90")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
