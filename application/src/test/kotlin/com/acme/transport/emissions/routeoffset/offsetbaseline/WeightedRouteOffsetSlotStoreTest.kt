package com.acme.transport.emissions.routeoffset.offsetbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRouteOffsetSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRouteOffsetSlotStore()
        val item = newWeightedRouteOffsetSlot("code-195", "label-853")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
