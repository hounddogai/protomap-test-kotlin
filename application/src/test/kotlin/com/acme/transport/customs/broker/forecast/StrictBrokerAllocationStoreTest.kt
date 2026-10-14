package com.acme.transport.customs.broker.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictBrokerAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictBrokerAllocationStore()
        val item = newStrictBrokerAllocation("code-828", "title-526")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
