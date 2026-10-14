package com.acme.transport.customs.hold.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldAllocationStore()
        val item = newHoldAllocation("code-45", "title-598")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
