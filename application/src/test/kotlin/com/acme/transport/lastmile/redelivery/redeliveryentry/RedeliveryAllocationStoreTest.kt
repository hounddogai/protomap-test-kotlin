package com.acme.transport.lastmile.redelivery.redeliveryentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryAllocationStore()
        val item = newRedeliveryAllocation("code-650", "name-466")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
