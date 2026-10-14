package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTrailerThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTrailerThresholdStore()
        val item = newWeightedTrailerThreshold("code-434", "name-758")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
