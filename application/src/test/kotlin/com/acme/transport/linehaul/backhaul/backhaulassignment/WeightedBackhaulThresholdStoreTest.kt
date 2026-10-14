package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedBackhaulThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedBackhaulThresholdStore()
        val item = newWeightedBackhaulThreshold("code-763", "name-764")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
