package com.acme.transport.fleetops.utilization.utilizationreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalUtilizationMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalUtilizationMappingStore()
        val item = newGlobalUtilizationMapping("code-255", "name-216")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
