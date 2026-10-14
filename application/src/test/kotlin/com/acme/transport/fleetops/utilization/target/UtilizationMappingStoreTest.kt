package com.acme.transport.fleetops.utilization.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationMappingStore()
        val item = newUtilizationMapping("code-210", "label-484")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
