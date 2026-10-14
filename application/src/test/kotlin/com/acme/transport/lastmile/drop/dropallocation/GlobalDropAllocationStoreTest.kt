package com.acme.transport.lastmile.drop.dropallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalDropAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalDropAllocationStore()
        val item = newGlobalDropAllocation("code-315", "title-969")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
