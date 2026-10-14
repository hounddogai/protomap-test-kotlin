package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedEvChargeGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedEvChargeGroupStore()
        val item = newWeightedEvChargeGroup("code-630", "title-901")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
