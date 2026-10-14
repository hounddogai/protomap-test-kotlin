package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingAvailabilityMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingAvailabilityMappingStore()
        val item = newRollingAvailabilityMapping("code-472", "name-182")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
