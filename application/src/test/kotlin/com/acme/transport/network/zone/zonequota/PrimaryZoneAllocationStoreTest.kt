package com.acme.transport.network.zone.zonequota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryZoneAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryZoneAllocationStore()
        val item = newPrimaryZoneAllocation("code-348", "title-448")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
