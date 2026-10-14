package com.acme.transport.linehaul.capacity.capacitypreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCapacityPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCapacityPreferenceStore()
        val item = newWeightedCapacityPreference("code-270", "name-181")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
