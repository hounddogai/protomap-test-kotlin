package com.acme.transport.crewing.availability.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAvailabilityManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAvailabilityManifestStore()
        val item = newAdaptiveAvailabilityManifest("code-819", "label-909")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
