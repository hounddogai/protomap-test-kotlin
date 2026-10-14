package com.acme.transport.network.zone.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedZoneTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedZoneTierStore()
        val item = newWeightedZoneTier("code-452", "label-845")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
