package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerManifestStore()
        val item = newTrailerManifest("code-806", "title-900")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
