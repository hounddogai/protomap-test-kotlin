package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedZoneManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedZoneManifestStore()
        val item = newWeightedZoneManifest("code-452", "label-650")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
