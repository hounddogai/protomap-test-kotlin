package com.acme.transport.terminal.traileryard.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardAllocationStore()
        val item = newTrailerYardAllocation("code-40", "label-721")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
