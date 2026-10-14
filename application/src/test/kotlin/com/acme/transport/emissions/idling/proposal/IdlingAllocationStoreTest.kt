package com.acme.transport.emissions.idling.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingAllocationStore()
        val item = newIdlingAllocation("code-663", "label-747")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
