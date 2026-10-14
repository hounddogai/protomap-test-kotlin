package com.acme.transport.customs.broker.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerAllocationStore()
        val item = newBrokerAllocation("code-978", "title-48")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
