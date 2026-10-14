package com.acme.transport.network.zone.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveZoneAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveZoneAllocationStore()
        val item = newAdaptiveZoneAllocation("code-582", "name-290")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
