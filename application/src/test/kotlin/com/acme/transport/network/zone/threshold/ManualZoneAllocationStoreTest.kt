package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualZoneAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualZoneAllocationStore()
        val item = newManualZoneAllocation("code-245", "name-426")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
