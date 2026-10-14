package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedLanePlanBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedLanePlanBundleStore()
        val item = newWeightedLanePlanBundle("code-295", "label-449")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
