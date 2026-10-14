package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRouteOffsetAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRouteOffsetAssignmentStore()
        val item = newBatchedRouteOffsetAssignment("code-143", "title-853")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
