package com.acme.transport.crewing.availability.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAvailabilityManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAvailabilityManifestStore()
        val item = newLenientAvailabilityManifest("code-942", "title-523")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
