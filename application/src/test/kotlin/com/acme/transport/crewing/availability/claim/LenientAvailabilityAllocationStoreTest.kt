package com.acme.transport.crewing.availability.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAvailabilityAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAvailabilityAllocationStore()
        val item = newLenientAvailabilityAllocation("code-200", "title-193")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
