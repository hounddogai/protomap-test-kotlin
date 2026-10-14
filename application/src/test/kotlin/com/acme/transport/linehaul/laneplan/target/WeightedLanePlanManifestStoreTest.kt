package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedLanePlanManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedLanePlanManifestStore()
        val item = newWeightedLanePlanManifest("code-558", "title-293")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
