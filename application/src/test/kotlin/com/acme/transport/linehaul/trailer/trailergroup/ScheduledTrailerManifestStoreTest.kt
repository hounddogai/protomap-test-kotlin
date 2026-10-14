package com.acme.transport.linehaul.trailer.trailergroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTrailerManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTrailerManifestStore()
        val item = newScheduledTrailerManifest("code-1", "title-63")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
