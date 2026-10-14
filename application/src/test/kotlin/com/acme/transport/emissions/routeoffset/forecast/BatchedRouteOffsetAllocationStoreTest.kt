package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRouteOffsetAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRouteOffsetAllocationStore()
        val item = newBatchedRouteOffsetAllocation("code-579", "title-92")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
